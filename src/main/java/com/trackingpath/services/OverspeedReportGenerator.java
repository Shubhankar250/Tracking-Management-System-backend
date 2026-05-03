package com.trackingpath.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.OverspeedReportDto;
import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component("overspeedreport")
public class OverspeedReportGenerator implements ReportGenerator {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    // ✅ HEADER MAP
    private static final Map<String, String> headerMap = new LinkedHashMap<>();

    static {
        headerMap.put("device", "Device Name");
        headerMap.put("speed", "Speed");
        headerMap.put("speed_limit", "Speed Limit");
        headerMap.put("address", "Address");
        headerMap.put("time", "Device Time");    
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

        String fileName = "overspeed_" + schedule.getId() + "_" + System.currentTimeMillis();

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

        String speedLimit = schedule.getSpeed_limit() != null
                ? schedule.getSpeed_limit().toString()
                : null;     
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

        // 🔥 skipColumns
        List<String> skipColumns =
                (schedule.getSkip_column() != null && !schedule.getSkip_column().isEmpty())
                        ? Arrays.stream(schedule.getSkip_column().split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList())
                        : Collections.emptyList();

        Set<String> finalColumns = skipColumns.isEmpty()
                ? new LinkedHashSet<>(headerMap.keySet())
                : headerMap.keySet().stream()
                .filter(col -> !skipColumns.contains(col))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 🔥 DATA (IMPORTANT: create this service method)
        List<OverspeedReportDto> dataList = reportService.getOverspeedReport(fromDate, toDate, deviceIds, speedLimit, user);

        // 🔥 FORMAT SWITCH
        if ("xlsx".equals(format)) {
            generateExcel(file, dataList, finalColumns);
        } else if ("csv".equals(format)) {
            generateCsv(file, dataList, finalColumns);
        } else if ("json".equals(format)) {
            generateJson(file, dataList, finalColumns);
        } else if ("html".equals(format)) {
            generateHtml(file, dataList, finalColumns);
        } else if ("pdf".equals(format)) {
            generatePdf(file, dataList, finalColumns);
        } else {
            throw new RuntimeException("Unsupported format: " + format);
        }

        return file;
    }

    // ================= CSV =================
    private void generateCsv(File file, List<OverspeedReportDto> list, Set<String> cols) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write(cols.stream().map(headerMap::get).collect(Collectors.joining(",")) + "\n");

            for (OverspeedReportDto d : list) {

                List<String> row = new ArrayList<>();

                for (String col : cols) {
                    switch (col) {
                        case "device_name": row.add(d.getDeviceName()); break;                       
                        case "speed": row.add(String.valueOf(d.getSpeed())); break;
                        case "speed_limit": row.add(String.valueOf(d.getSpeedLimit())); break;                       
                        case "address": row.add(d.getAddress()); break;
                        case "time": row.add(d.getDeviceTime()); break;                       
                    }
                }

                writer.write(String.join(",", row) + "\n");
            }
        }
    }

    // ================= EXCEL =================
    private void generateExcel(File file, List<OverspeedReportDto> list, Set<String> cols) throws Exception {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Overspeed Report");

        Row header = sheet.createRow(0);

        Map<String, Integer> colPos = new HashMap<>();
        int i = 0;

        for (String col : cols) {
            header.createCell(i).setCellValue(headerMap.get(col));
            colPos.put(col, i++);
        }

        int rowNum = 1;

        for (OverspeedReportDto d : list) {
            Row row = sheet.createRow(rowNum++);

            for (String col : cols) {
                int c = colPos.get(col);

                switch (col) {
                    case "device_name": row.createCell(c).setCellValue(d.getDeviceName()); break;                  
                    case "speed": row.createCell(c).setCellValue(d.getSpeed()); break;
                    case "speed_limit": row.createCell(c).setCellValue(d.getSpeedLimit()); break;                  
                    case "address": row.createCell(c).setCellValue(d.getAddress()); break;
                    case "time": row.createCell(c).setCellValue(d.getDeviceTime()); break;                  
                }
            }
        }

        for (int j = 0; j < i; j++) sheet.autoSizeColumn(j);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
        }

        wb.close();
    }

    // ================= JSON =================
    private void generateJson(File file, List<OverspeedReportDto> list, Set<String> cols) throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();

        for (OverspeedReportDto d : list) {

            Map<String, Object> row = new LinkedHashMap<>();

            for (String col : cols) {
                switch (col) {
                    case "device": row.put("device_name", d.getDeviceName()); break;                   
                    case "speed": row.put("speed", d.getSpeed()); break;
                    case "speed_limit": row.put("speed_limit", d.getSpeedLimit()); break;
                    case "address": row.put("address", d.getAddress()); break;
                    case "time": row.put("deviceTime", d.getDeviceTime()); break;
                 
                }
            }

            result.add(row);
        }

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, result);
    }

    // ================= HTML =================
    private void generateHtml(File file, List<OverspeedReportDto> list, Set<String> cols) throws Exception {

        try (FileWriter w = new FileWriter(file)) {

            w.write("<html><body><table border='1'>");

            w.write("<tr>");
            for (String col : cols) {
                w.write("<th>" + headerMap.get(col) + "</th>");
            }
            w.write("</tr>");

            for (OverspeedReportDto d : list) {
                w.write("<tr>");

                for (String col : cols) {
                    String val = "";

                    switch (col) {
                        case "device_name": val = d.getDeviceName(); break;                      
                        case "speed": val = String.valueOf(d.getSpeed()); break;
                        case "speed_limit": val = String.valueOf(d.getSpeedLimit()); break;                       
                        case "address": val = d.getAddress(); break;
                        case "time": val = d.getDeviceTime(); break;
                      
                    }

                    w.write("<td>" + val + "</td>");
                }

                w.write("</tr>");
            }

            w.write("</table></body></html>");
        }
    }

    // ================= PDF =================
    private void generatePdf(File file, List<OverspeedReportDto> list, Set<String> cols) throws Exception {

        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdf);

        com.itextpdf.layout.element.Table table =
                new com.itextpdf.layout.element.Table(cols.size());

        for (String col : cols) {
            table.addHeaderCell(headerMap.get(col));
        }

        for (OverspeedReportDto d : list) {
            for (String col : cols) {

                String val = "";

                switch (col) {
                    case "device": val = d.getDeviceName(); break;                  
                    case "speed": val = String.valueOf(d.getSpeed()); break;
                    case "speed_limit": val = String.valueOf(d.getSpeedLimit()); break;                   
                    case "address": val = d.getAddress(); break;
                    case "time": val = d.getDeviceTime(); break;                 
                }

                table.addCell(val);
            }
        }

        doc.add(table);
        doc.close();
    }
}
