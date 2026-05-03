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
import com.trackingpath.dtos.DailySummaryReportDTO;
import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;

@Component("dailysummaryreport")
public class DailySummaryReportGenerator implements ReportGenerator{

	@Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    // ✅ Header Mapping
    private static final Map<String, String> headerMap = new LinkedHashMap<>();

    static {
        headerMap.put("date", "Date");
        headerMap.put("movement", "Movement Time");
        headerMap.put("idle", "Idle Time");
    }
    
 // ✅ VALUE MAPPING (MAKE IT STATIC)
    private static final Map<String, java.util.function.Function<DailySummaryReportDTO, String>> valueMap = new LinkedHashMap<>();

    static {
        valueMap.put("date", d -> d.getDate() != null ? d.getDate() : "");
        valueMap.put("movement", d -> d.getTotal_movement_time() != null ? d.getTotal_movement_time() : "");
        valueMap.put("idle", d -> d.getTotal_idle_time() != null ? d.getTotal_idle_time() : "");
    }
    
    private String safe(String val) {
        return val != null ? val : "";
    }

    @Override
    public File generate(ReportSchedule schedule) throws Exception {

        File dir = new File("generated-reports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Unable to create directory");
        }

        String format = schedule.getOutputFormat() != null
                ? schedule.getOutputFormat().toLowerCase()
                : "csv";

        String fileName = "daily_summary_" + schedule.getId() + "_" + System.currentTimeMillis();

        String extension = switch (format) {
            case "xlsx" -> ".xlsx";
            case "csv" -> ".csv";
            case "json" -> ".json";
            case "html" -> ".html";
            case "pdf" -> ".pdf";
            default -> ".txt";
        };

        File file = new File(dir, fileName + extension);

        // 👤 Get User
        Users user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 Filters
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
                deviceIds = new ObjectMapper().readValue(raw, new TypeReference<List<Long>>() {});
            } else {
            	deviceIds = Arrays.stream(raw.split(","))
            	        .map(String::trim)
            	        .filter(s -> !s.isEmpty())
            	        .map(Long::parseLong)
            	        .collect(Collectors.toList());
            }
        }
        
        List<String> skipColumns =
                (schedule.getSkip_column() != null && !schedule.getSkip_column().isEmpty())
                        ? Arrays.stream(schedule.getSkip_column().split(","))
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .collect(Collectors.toList())
                        : new ArrayList<>();

        List<String> finalColumns = headerMap.keySet().stream()
                .filter(col -> !skipColumns.contains(col))
                .toList();

        // 🔥 DATA FETCH
        List<DailySummaryReportDTO> dataList = new ArrayList<>();

        for (Long deviceId : deviceIds) {
            dataList.addAll(
                reportService.getDailySummaryReport(deviceId, fromDate, toDate, user)
            );
        }
        
        System.out.println("Device IDs: " + deviceIds);
        System.out.println("From: " + fromDate + " To: " + toDate);
        System.out.println("Data size: " + dataList.size());

        // 🔥 FORMAT SWITCH
        switch (format) {
            case "xlsx" -> generateExcel(file, dataList, finalColumns);
            case "csv" -> generateCsv(file, dataList, finalColumns);
            case "json" -> generateJson(file, dataList, finalColumns);
            case "html" -> generateHtml(file, dataList, finalColumns);
            case "pdf" -> generatePdf(file, dataList, finalColumns);
            default -> throw new RuntimeException("Unsupported format: " + format);
        }

        return file;
    }

    // ================= CSV =================
    private void generateCsv(File file, List<DailySummaryReportDTO> list, List<String> cols) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write(
                cols.stream()
                    .map(headerMap::get)
                    .collect(Collectors.joining(",")) + "\n"
            );

            for (DailySummaryReportDTO d : list) {

                List<String> row = new ArrayList<>();

                for (String col : cols) {
                	row.add(valueMap.get(col).apply(d));
                }

                writer.write(String.join(",", row) + "\n");
            }
        }
    }

    // ================= EXCEL =================
    private void generateExcel(File file, List<DailySummaryReportDTO> list, List<String> cols) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Daily Summary");

        Row header = sheet.createRow(0);

        Map<String, Integer> colIndex = new HashMap<>();
        int col = 0;

        for (String c : cols) {
            header.createCell(col).setCellValue(headerMap.get(c));
            colIndex.put(c, col++);
        }

        int rowNum = 1;

        for (DailySummaryReportDTO d : list) {
            Row row = sheet.createRow(rowNum++);

            for (String c : cols) {
                int i = colIndex.get(c);

                row.createCell(i).setCellValue(valueMap.get(c).apply(d));
            }
        }

        for (int i = 0; i < cols.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();
    }

    // ================= JSON =================
    private void generateJson(File file, List<DailySummaryReportDTO> list, List<String> cols) throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();

        for (DailySummaryReportDTO data : list) {

            Map<String, Object> row = new LinkedHashMap<>();

            for (String c : cols) {
            	row.put(headerMap.get(c), valueMap.get(c).apply(data));
            }

            result.add(row);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, result);
    }

    // ================= HTML =================
    private void generateHtml(File file, List<DailySummaryReportDTO> list, List<String> cols) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write("<html><body><table border='1'>");

            // ✅ HEADER
            writer.write("<tr>");
            for (String c : cols) {
                writer.write("<th>" + headerMap.get(c) + "</th>");
            }
            writer.write("</tr>");

            // ✅ DATA
            for (DailySummaryReportDTO d : list) {
                writer.write("<tr>");

                for (String c : cols) {
                	String val = valueMap.get(c).apply(d);

                    writer.write("<td>" + val + "</td>");
                }

                writer.write("</tr>");
            }

            writer.write("</table></body></html>");
        }
    }

    // ================= PDF =================
    private void generatePdf(File file, List<DailySummaryReportDTO> list, List<String> cols) throws Exception {

        com.itextpdf.kernel.pdf.PdfWriter writer =
                new com.itextpdf.kernel.pdf.PdfWriter(file);

        com.itextpdf.kernel.pdf.PdfDocument pdf =
                new com.itextpdf.kernel.pdf.PdfDocument(writer);

        com.itextpdf.layout.Document document =
                new com.itextpdf.layout.Document(pdf);

        // ✅ Use dynamic column size
        com.itextpdf.layout.element.Table table =
                new com.itextpdf.layout.element.Table(cols.size());

        // ✅ Full width table
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        // ================= HEADER =================
        com.itextpdf.kernel.font.PdfFont boldFont =
                com.itextpdf.kernel.font.PdfFontFactory.createFont(
                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
                );

        for (String c : cols) {
            table.addHeaderCell(
                new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph(headerMap.get(c))
                        .setFont(boldFont))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceGray(0.85f))
                    .setPadding(5)
            );
        }

        // ================= DATA =================
        for (DailySummaryReportDTO d : list) {

            for (String c : cols) {

            	String val = valueMap.get(c).apply(d);

                table.addCell(
                    new com.itextpdf.layout.element.Cell()
                        .add(new com.itextpdf.layout.element.Paragraph(val))
                );
            }
        }

        document.add(table);
        document.close();
    }

}
