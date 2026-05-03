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

import com.trackingpath.dtos.DailySummaryReportDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DailySummaryReportExcelData")
public class DailySummaryReportExcelData extends AbstractXlsxStreamingView {
	
	@Override
    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response) {

        List<DailySummaryReportDTO> data =
                (List<DailySummaryReportDTO>) model.get("DailySummaryReport");

        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns");

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("date", "Date");
        headerMap.put("movement", "Movement Time");
        headerMap.put("idle", "Idle Time");

        Set<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? new LinkedHashSet<>(headerMap.keySet())
                : headerMap.keySet().stream()
                .filter(col -> !selectedColumns.contains(col))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Sheet sheet = workbook.createSheet("DailySummaryReport");

        // Header style
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

        for (DailySummaryReportDTO d : data) {
            Row row = sheet.createRow(rowNum++);

            for (String col : finalColumns) {
                int i = colPos.get(col);

                switch (col) {
                    case "date": row.createCell(i).setCellValue(d.getDate()); break;
                    case "movement": row.createCell(i).setCellValue(d.getTotal_movement_time()); break;
                    case "idle": row.createCell(i).setCellValue(d.getTotal_idle_time()); break;
                }
            }
        }

        for (int i = 0; i < colIndex; i++) {
            sheet.setColumnWidth(i, 6000);
        }
    }

}
