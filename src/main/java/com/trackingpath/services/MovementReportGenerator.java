package com.trackingpath.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.EventDataBean;
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

@Component("movementreport")
public class MovementReportGenerator implements ReportGenerator {

    @Autowired
    private ReportService reportService;
    @Autowired
    private UserRepository userRepository;
    // ✅ GLOBAL HEADER MAP (fix scope issue)
    private static final Map<String, String> headerMap = new LinkedHashMap<>();

    static {
        headerMap.put("name", "Name");
        headerMap.put("time", "Time");
        headerMap.put("latitude", "Latitude");
        headerMap.put("longitude", "Longitude");
        headerMap.put("speed", "Speed");
        headerMap.put("address", "Address");
        headerMap.put("distance", "Distance");
    }

    @Override
    public File generate(ReportSchedule schedule) throws Exception {

        // 📁 folder
        File dir = new File("generated-reports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Unable to create report directory");
        }

        // 📄 format
        String format = schedule.getOutputFormat() != null
                ? schedule.getOutputFormat().toLowerCase()
                : "csv";

        String fileName = "movement_" + schedule.getId() + "_" + System.currentTimeMillis();
        String extension = switch (format) {
        case "xlsx" -> ".xlsx";
        case "csv" -> ".csv";
        case "json" -> ".json";
        case "html" -> ".html";
        case "pdf" -> ".pdf";
        default -> ".txt";
    };

    File file = new File(dir, fileName + extension);

        // 👤 user
        Users user = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 filters
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
        String speed = schedule.getSpeed_limit() != null ? schedule.getSpeed_limit().toString() : null;     
        List<Long> deviceIds = new ArrayList<>();

        if (schedule.getDevices() != null && !schedule.getDevices().isEmpty()) {

            String raw = schedule.getDevices().trim();    

            if (raw.startsWith("[") && raw.endsWith("]")) {
                // ✅ JSON array case
                deviceIds = mapper.readValue(raw, new TypeReference<List<Long>>() {});
            } else {
                // ✅ Comma separated case
                deviceIds = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }
        }

        List<Long> geofences = new ArrayList<>();

        if (schedule.getGeofences() != null) {

            try {
                Object geo = schedule.getGeofences();

                if (geo instanceof String) {
                    String raw = ((String) geo).replace("[", "").replace("]", "");
                    geofences = Arrays.stream(raw.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                } else {
                    geofences = mapper.convertValue(geo, new TypeReference<List<Long>>() {});
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 🔥 skipColumns
        List<String> skipColumns =
                (schedule.getSkip_column() != null && !schedule.getSkip_column().isEmpty())
                        ? Arrays.stream(schedule.getSkip_column().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(String::toLowerCase)   // 🔥 FIX
                                .collect(Collectors.toList())
                        : Collections.emptyList();

        // 🔥 FINAL COLUMNS (IMPORTANT)
        Set<String> finalColumns;

        if (skipColumns.isEmpty()) {
            finalColumns = new LinkedHashSet<>(headerMap.keySet());
        } else {
            finalColumns = headerMap.keySet().stream()
                    .filter(col -> !skipColumns.contains(col))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (finalColumns.isEmpty()) {
                finalColumns = new LinkedHashSet<>(headerMap.keySet());
            }
        }

        // 🔥 DATA
        List<EventDataBean> dataList = reportService.getMovementReport(
                fromDate, toDate, deviceIds, geofences, speed, user
        );

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
    private void generateCsv(File file, List<EventDataBean> list, Set<String> finalColumns) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            // Header
            writer.write(
                    finalColumns.stream()
                            .map(headerMap::get)
                            .collect(Collectors.joining(",")) + "\n"
            );

            // Data
            for (EventDataBean data : list) {

                List<String> row = new ArrayList<>();

                for (String col : finalColumns) {
                    switch (col) {
                        case "name":
                            row.add(data.getDetails() != null ? data.getDetails().getName() : "");
                            break;
                        case "time":
                            row.add(data.getDeviceTime());
                            break;
                        case "latitude":
                            row.add(String.valueOf(data.getLatitude()));
                            break;
                        case "longitude":
                            row.add(String.valueOf(data.getLongitude()));
                            break;
                        case "speed":
                            row.add(String.valueOf(data.getSpeed()));
                            break;
                        case "address":
                            row.add(data.getAddress());
                            break;
                        case "distance":
                            row.add(String.valueOf(data.getDistance()));
                            break;
                    }
                }

                writer.write(String.join(",", row) + "\n");
            }
        }
    }

    // ================= EXCEL =================
    private void generateExcel(File file, List<EventDataBean> list, Set<String> finalColumns) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movement Report");

        Row header = sheet.createRow(0);

        Map<String, Integer> colPos = new HashMap<>();
        int colIndex = 0;

        // Header
        for (String col : finalColumns) {
            header.createCell(colIndex).setCellValue(headerMap.get(col));
            colPos.put(col, colIndex++);
        }

        // Data
        int rowNum = 1;

        for (EventDataBean data : list) {

            Row row = sheet.createRow(rowNum++);

            for (String col : finalColumns) {
                int i = colPos.get(col);

                switch (col) {
                    case "name":
                        row.createCell(i).setCellValue(
                                data.getDetails() != null ? data.getDetails().getName() : ""
                        );
                        break;
                    case "time":
                        row.createCell(i).setCellValue(data.getDeviceTime());
                        break;
                    case "latitude":
                        row.createCell(i).setCellValue(data.getLatitude());
                        break;
                    case "longitude":
                        row.createCell(i).setCellValue(data.getLongitude());
                        break;
                    case "speed":
                        row.createCell(i).setCellValue(data.getSpeed());
                        break;
                    case "address":
                        row.createCell(i).setCellValue(data.getAddress());
                        break;
                    case "distance":
                        row.createCell(i).setCellValue(data.getDistance());
                        break;
                }
            }
        }

        for (int i = 0; i < colIndex; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();
    }
    private void generateJson(File file, List<EventDataBean> list, Set<String> finalColumns) throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();

        for (EventDataBean data : list) {
            Map<String, Object> row = new LinkedHashMap<>();

            for (String col : finalColumns) {
                switch (col) {
                    case "name":
                        row.put("name", data.getDetails() != null ? data.getDetails().getName() : "");
                        break;
                    case "time":
                        row.put("time", data.getDeviceTime());
                        break;
                    case "latitude":
                        row.put("latitude", data.getLatitude());
                        break;
                    case "longitude":
                        row.put("longitude", data.getLongitude());
                        break;
                    case "speed":
                        row.put("speed", data.getSpeed());
                        break;
                    case "address":
                        row.put("address", data.getAddress());
                        break;
                    case "distance":
                        row.put("distance", data.getDistance());
                        break;
                }
            }

            result.add(row);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, result);
    }
    private void generateHtml(File file, List<EventDataBean> list, Set<String> finalColumns) throws Exception {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write("<html><body><table border='1'>");

            // Header
            writer.write("<tr>");
            for (String col : finalColumns) {
                writer.write("<th>" + headerMap.get(col) + "</th>");
            }
            writer.write("</tr>");

            // Data
            for (EventDataBean data : list) {
                writer.write("<tr>");

                for (String col : finalColumns) {
                    String value = "";

                    switch (col) {
                        case "name":
                            value = data.getDetails() != null ? data.getDetails().getName() : "";
                            break;
                        case "time":
                            value = data.getDeviceTime();
                            break;
                        case "latitude":
                            value = String.valueOf(data.getLatitude());
                            break;
                        case "longitude":
                            value = String.valueOf(data.getLongitude());
                            break;
                        case "speed":
                            value = String.valueOf(data.getSpeed());
                            break;
                        case "address":
                            value = data.getAddress();
                            break;
                        case "distance":
                            value = String.valueOf(data.getDistance());
                            break;
                    }

                    writer.write("<td>" + value + "</td>");
                }

                writer.write("</tr>");
            }

            writer.write("</table></body></html>");
        }
    }
    private void generatePdf(File file, List<EventDataBean> list, Set<String> finalColumns) throws Exception {

        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

        com.itextpdf.layout.element.Table table =
                new com.itextpdf.layout.element.Table(finalColumns.size());

        // Header
        for (String col : finalColumns) {
            table.addHeaderCell(headerMap.get(col));
        }

        // Data
        for (EventDataBean data : list) {
            for (String col : finalColumns) {

                String value = "";

                switch (col) {
                    case "name":
                        value = data.getDetails() != null ? data.getDetails().getName() : "";
                        break;
                    case "time":
                        value = data.getDeviceTime();
                        break;
                    case "latitude":
                        value = String.valueOf(data.getLatitude());
                        break;
                    case "longitude":
                        value = String.valueOf(data.getLongitude());
                        break;
                    case "speed":
                        value = String.valueOf(data.getSpeed());
                        break;
                    case "address":
                        value = data.getAddress();
                        break;
                    case "distance":
                        value = String.valueOf(data.getDistance());
                        break;
                }

                table.addCell(value);
            }
        }

        document.add(table);
        document.close();
    }
}