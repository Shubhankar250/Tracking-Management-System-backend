package com.trackingpath.export;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import com.trackingpath.dtos.OverspeedReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("OverspeedReportExcelData")
public class OverspeedReportExcelData extends AbstractXlsxStreamingView {
	
	@Override
    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response) {

        List<OverspeedReportDto> data =
                (List<OverspeedReportDto>) model.get("OverspeedReport");

        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns");

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("device", "Device");
        headerMap.put("speed", "Speed");
        headerMap.put("speed_limit", "Speed Limit");
        headerMap.put("address", "Address");
        headerMap.put("time", "Device Time");

        Set<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? new LinkedHashSet<>(headerMap.keySet())
                : headerMap.keySet().stream()
                    .filter(col -> !selectedColumns.contains(col))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        Sheet sheet = workbook.createSheet("OverspeedReport");
        
     // ✅ HEADER STYLE
        CellStyle headerStyle = workbook.createCellStyle();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // light grey background
        headerStyle.setFillForegroundColor((short) 22); // light gray
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        // center align
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        Map<String, Integer> colPos = new HashMap<>();

        int colIndex = 0;
        for (String col : finalColumns) {
            Cell cell = headerRow.createCell(colIndex);
            cell.setCellValue(headerMap.get(col));
            cell.setCellStyle(headerStyle); // ✅ APPLY STYLE
            colPos.put(col, colIndex++);
        }

        int rowNum = 1;
        for (OverspeedReportDto d : data) {
            Row row = sheet.createRow(rowNum++);
            for (String col : finalColumns) {
                int i = colPos.get(col);
                switch (col) {
                    case "device": row.createCell(i).setCellValue(d.getDeviceName()); break;
                    case "speed": row.createCell(i).setCellValue(d.getSpeed()); break;
                    case "speed_limit": row.createCell(i).setCellValue(d.getSpeedLimit()); break;
                    case "address": row.createCell(i).setCellValue(d.getAddress()); break;
                    case "time": row.createCell(i).setCellValue(d.getDeviceTime()); break;
                }
            }
        }

        for (int i = 0; i < colIndex; i++) {
            sheet.setColumnWidth(i, 6000);
        }
    }

}
