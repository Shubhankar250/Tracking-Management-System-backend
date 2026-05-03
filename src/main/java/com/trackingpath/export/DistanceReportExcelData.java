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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import com.trackingpath.dtos.DistanceReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DistanceReportExcelData")
public class DistanceReportExcelData extends AbstractXlsxStreamingView {
	
	@Override
    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response) {

        List<DistanceReportDto> data =
                (List<DistanceReportDto>) model.get("DistanceReport");

        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns");

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("device", "Device");
        headerMap.put("start", "Start Address");
        headerMap.put("end", "End Address");
        headerMap.put("stime", "Start Time");
        headerMap.put("etime", "End Time");
        headerMap.put("distance", "Distance");

        Set<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? new LinkedHashSet<>(headerMap.keySet())
                : headerMap.keySet().stream()
                .filter(col -> !selectedColumns.contains(col))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        Sheet sheet = workbook.createSheet("DistanceReport");

        // ✅ HEADER STYLE
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor((short) 22);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
            cell.setCellStyle(headerStyle);
            colPos.put(col, colIndex++);
        }

        int rowNum = 1;

        for (DistanceReportDto d : data) {
            Row row = sheet.createRow(rowNum++);
            for (String col : finalColumns) {
                int i = colPos.get(col);
                switch (col) {
                    case "device": row.createCell(i).setCellValue(d.getName()); break;
                    case "start": row.createCell(i).setCellValue(d.getStartPoint()); break;
                    case "end": row.createCell(i).setCellValue(d.getEndPoint()); break;
                    case "stime": row.createCell(i).setCellValue(d.getStime()); break;
                    case "etime": row.createCell(i).setCellValue(d.getEtime()); break;
                    case "distance": row.createCell(i).setCellValue(d.getDistance()); break;
                }
            }
        }

        for (int i = 0; i < colIndex; i++) {
            sheet.setColumnWidth(i, 6000);
        }
    }

}
