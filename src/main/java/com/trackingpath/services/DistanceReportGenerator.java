package com.trackingpath.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DistanceReportDto;
import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;

@Component("distancereport")
public class DistanceReportGenerator implements ReportGenerator {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private UserRepository userRepository;
	
	private static final Map<String, String> headerMap = new LinkedHashMap<>();

    static {
        headerMap.put("device", "Device");
        headerMap.put("start", "Start Address");
        headerMap.put("end", "End Address");
        headerMap.put("stime", "Start Time");
        headerMap.put("etime", "End Time");
        headerMap.put("distance", "Distance");
    }

    @Override
    public File generate(ReportSchedule schedule) throws Exception {
      
        File dir = new File("generated-reports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Unable to create report directory");
        }

        String format = schedule.getOutputFormat() != null
                ? schedule.getOutputFormat().toLowerCase()
                : "csv";

        String fileName = "distance_" + schedule.getId() + "_" + System.currentTimeMillis();

        String extension = switch (format) {
            case "xlsx" -> ".xlsx";
            case "csv" -> ".csv";
            case "json" -> ".json";
            case "html" -> ".html";
            case "pdf" -> ".pdf";
            default -> ".txt";
        };

        File file = new File(dir, fileName + extension);

        Users user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> filters = new HashMap<>();

        if (schedule.getFilterJson() != null && !schedule.getFilterJson().isEmpty()) {
            filters = mapper.readValue(schedule.getFilterJson(), new TypeReference<>() {});
        }

        String fromDate = (String) filters.getOrDefault("from_date", schedule.getFrom_date());
        String toDate = (String) filters.getOrDefault("to_date", schedule.getTo_date());

        if (schedule.getDaily() != null && !"false".equalsIgnoreCase(schedule.getDaily())) {

            String today = new java.text.SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date());

            fromDate = today;
            toDate = today;
        }
     // 🔥 WEEKLY custom range (selected day → next 6 days)
        if (schedule.getWeekly() != null && !"false".equalsIgnoreCase(schedule.getWeekly())) {

            String weekly = schedule.getWeekly(); // e.g. "tue_01:01"
            String day = weekly.split("_")[0].toLowerCase();

            Map<String, Integer> dayMap = new HashMap<>();
            dayMap.put("sun", Calendar.SUNDAY);
            dayMap.put("mon", Calendar.MONDAY);
            dayMap.put("tue", Calendar.TUESDAY);
            dayMap.put("wed", Calendar.WEDNESDAY);
            dayMap.put("thu", Calendar.THURSDAY);
            dayMap.put("fri", Calendar.FRIDAY);
            dayMap.put("sat", Calendar.SATURDAY);

            int targetDay = dayMap.getOrDefault(day, Calendar.MONDAY);

            Calendar cal = Calendar.getInstance();

            // 🔥 go to previous week
            cal.add(Calendar.WEEK_OF_YEAR, -1);

            // 🔥 set to selected day (e.g. Tuesday)
            cal.set(Calendar.DAY_OF_WEEK, targetDay);
            Date from = cal.getTime();

            // 🔥 to = +6 days (Tue → Mon)
            cal.add(Calendar.DAY_OF_MONTH, 6);
            Date to = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            fromDate = sdf.format(from);
            toDate = sdf.format(to);
        }
     // 🔥 MONTHLY custom logic
        if (schedule.getMonthly() != null && !"false".equalsIgnoreCase(schedule.getMonthly())) {

            String monthly = schedule.getMonthly(); // e.g. "30_10:20"
            String[] parts = monthly.split("_");

            int selectedDate = Integer.parseInt(parts[0]);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Calendar now = Calendar.getInstance();

            if (selectedDate == 1) {
                // ✅ Full previous month

                Calendar prev = (Calendar) now.clone();
                prev.add(Calendar.MONTH, -1);

                prev.set(Calendar.DAY_OF_MONTH, 1);
                Date from = prev.getTime();

                prev.set(Calendar.DAY_OF_MONTH, prev.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date to = prev.getTime();

                fromDate = sdf.format(from);
                toDate = sdf.format(to);

            } else {

                // 🔥 FROM → previous month (safe date)
                Calendar prev = (Calendar) now.clone();
                prev.add(Calendar.MONTH, -1);

                int prevMax = prev.getActualMaximum(Calendar.DAY_OF_MONTH);
                int safeFromDay = Math.min(selectedDate, prevMax); // ✅ FIX

                prev.set(Calendar.DAY_OF_MONTH, safeFromDay);
                Date from = prev.getTime();

                // 🔥 TO → current month (safe date -1)
                Calendar curr = (Calendar) now.clone();

                int currMax = curr.getActualMaximum(Calendar.DAY_OF_MONTH);
                int safeToDay = Math.min(selectedDate - 1, currMax); // ✅ FIX

                curr.set(Calendar.DAY_OF_MONTH, safeToDay);
                Date to = curr.getTime();

                fromDate = sdf.format(from);
                toDate = sdf.format(to);
            }
        }

        List<Long> deviceIds = new ArrayList<>();

        if (schedule.getDevices() != null && !schedule.getDevices().isEmpty()) {

            String raw = schedule.getDevices().trim();

            if (raw.startsWith("[") && raw.endsWith("]")) {
                deviceIds = mapper.readValue(raw, new TypeReference<List<Long>>() {});
            } else {
                deviceIds = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }
        }

        List<DistanceReportDto> dataList = new ArrayList<>();

        for (Long deviceId : deviceIds) {
            dataList.addAll(
                    reportService.distanceReport(deviceId, fromDate, toDate, user)
            );
        }

        switch (format) {
            case "xlsx" -> generateExcel(file, dataList);
            case "csv" -> generateCsv(file, dataList);
            case "json" -> generateJson(file, dataList);
            case "html" -> generateHtml(file, dataList);
            case "pdf" -> generatePdf(file, dataList);
            default -> throw new RuntimeException("Unsupported format: " + format);
        }

        return file;
    }

    // ================= CSV =================
    private void generateCsv(File file, List<DistanceReportDto> list) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write(String.join(",", headerMap.values()) + "\n");

            for (DistanceReportDto d : list) {
                writer.write(String.join(",",
                        d.getName(),
                        d.getStartPoint(),
                        d.getEndPoint(),
                        d.getStime(),
                        d.getEtime(),
                        d.getDistance()
                ) + "\n");
            }
        }
    }

    // ================= EXCEL =================
    private void generateExcel(File file, List<DistanceReportDto> list) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Distance Report");

        Row header = sheet.createRow(0);

        int col = 0;
        for (String h : headerMap.values()) {
            header.createCell(col++).setCellValue(h);
        }

        int rowNum = 1;

        for (DistanceReportDto d : list) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(d.getName());
            row.createCell(1).setCellValue(d.getStartPoint());
            row.createCell(2).setCellValue(d.getEndPoint());
            row.createCell(3).setCellValue(d.getStime());
            row.createCell(4).setCellValue(d.getEtime());
            row.createCell(5).setCellValue(d.getDistance());
        }

        for (int i = 0; i < headerMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();
    }

    // ================= JSON =================
    private void generateJson(File file, List<DistanceReportDto> list) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
    }

    // ================= HTML =================
    private void generateHtml(File file, List<DistanceReportDto> list) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write("<html><body><table border='1'>");

            writer.write("<tr>");
            for (String h : headerMap.values()) {
                writer.write("<th>" + h + "</th>");
            }
            writer.write("</tr>");

            for (DistanceReportDto d : list) {
                writer.write("<tr>");
                writer.write("<td>" + d.getName() + "</td>");
                writer.write("<td>" + d.getStartPoint() + "</td>");
                writer.write("<td>" + d.getEndPoint() + "</td>");
                writer.write("<td>" + d.getStime() + "</td>");
                writer.write("<td>" + d.getEtime() + "</td>");
                writer.write("<td>" + d.getDistance() + "</td>");
                writer.write("</tr>");
            }

            writer.write("</table></body></html>");
        }
    }

    // ================= PDF =================
    private void generatePdf(File file, List<DistanceReportDto> list) throws Exception {

        com.itextpdf.kernel.pdf.PdfWriter writer =
                new com.itextpdf.kernel.pdf.PdfWriter(file);

        com.itextpdf.kernel.pdf.PdfDocument pdf =
                new com.itextpdf.kernel.pdf.PdfDocument(writer);

        com.itextpdf.layout.Document document =
                new com.itextpdf.layout.Document(pdf);

        com.itextpdf.layout.element.Table table =
                new com.itextpdf.layout.element.Table(headerMap.size());

        // Header
        for (String h : headerMap.values()) {
            table.addHeaderCell(h);
        }

        // Data
        for (DistanceReportDto d : list) {
            table.addCell(d.getName());
            table.addCell(d.getStartPoint());
            table.addCell(d.getEndPoint());
            table.addCell(d.getStime());
            table.addCell(d.getEtime());
            table.addCell(d.getDistance());
        }

        document.add(table);
        document.close();
    }

}
