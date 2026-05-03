package com.trackingpath.export;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import com.trackingpath.dtos.EventDataBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("MovementReportExcelData")
public class MovementReportExcelData extends AbstractXlsxStreamingView {

    @Override
    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response) {

        @SuppressWarnings("unchecked")
        List<EventDataBean> data =
                (List<EventDataBean>) model.get("MovementReport");

        @SuppressWarnings("unchecked")
        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns");

        // Header mapping
        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("name", "Name");
        headerMap.put("time", "Time");
        headerMap.put("latitude", "Latitude");
        headerMap.put("longitude", "Longitude");
        headerMap.put("speed", "Speed");
        headerMap.put("address", "Address");
        headerMap.put("distance", "Distance");

        // Decide final columns
        Set<String> finalColumns;
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            finalColumns = new LinkedHashSet<>(headerMap.keySet());
        } else {
            finalColumns = headerMap.keySet().stream()
                    .filter(col -> !selectedColumns.contains(col))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (finalColumns.isEmpty()) {
                finalColumns = new LinkedHashSet<>(headerMap.keySet());
            }
        }

        // Create sheet
        Sheet sheet = workbook.createSheet("MovementReport");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Header row
        Row headerRow = sheet.createRow(0);
        Map<String, Integer> colPos = new HashMap<>();

        int colIndex = 0;
        for (String col : finalColumns) {
            Cell cell = headerRow.createCell(colIndex);
            cell.setCellValue(headerMap.get(col));
            cell.setCellStyle(headerStyle);
            colPos.put(col, colIndex++);
        }

        // Data rows
        int rowNum = 1;
        if (data != null) {
            for (EventDataBean entry : data) {
                Row row = sheet.createRow(rowNum++);
                for (String col : finalColumns) {
                    int i = colPos.get(col);
                    switch (col) {
                        case "name":
                            row.createCell(i).setCellValue(
                                    entry.getDetails() != null
                                            ? entry.getDetails().getName()
                                            : "");
                            break;
                        case "time":
                            row.createCell(i).setCellValue(entry.getDeviceTime());
                            break;
                        case "latitude":
                            row.createCell(i).setCellValue(entry.getLatitude());
                            break;
                        case "longitude":
                            row.createCell(i).setCellValue(entry.getLongitude());
                            break;
                        case "speed":
                            row.createCell(i).setCellValue(entry.getSpeed());
                            break;
                        case "address":
                            row.createCell(i).setCellValue(entry.getAddress());
                            break;
                        case "distance":
                            row.createCell(i).setCellValue(entry.getDistance());
                            break;
                    }
                }
            }
        }

        // ✅ OLD POI SAFE: fixed column widths (no autoSize)
        for (int i = 0; i < colIndex; i++) {
            sheet.setColumnWidth(i, 6000); // ~25 characters
        }
    }
}
