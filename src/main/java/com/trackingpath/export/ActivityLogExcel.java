package com.trackingpath.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import com.trackingpath.dtos.ActivityLogDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public class ActivityLogExcel extends AbstractXlsxStreamingView {

    @Override
    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<ActivityLogDTO> data =
                (List<ActivityLogDTO>) model.get("ActivityLogDataEXCEL");

        String date = (String) model.get("date");

        /* ================= SHEET ================= */

        Sheet sheet = workbook.createSheet("Activity Log Data");

        // 🔥 CAST REQUIRED FOR STREAMING EXCEL
        SXSSFSheet sxssfSheet = (SXSSFSheet) sheet;

        /* ================= STYLES ================= */

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        /* ================= TITLE ================= */

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Activity Log Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        /* ================= DATE ================= */

        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Date : " + date);
        dateCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        /* ================= HEADERS ================= */

        String[] columns = {
                "Log Type",
                "Message",
                "User Agent",
                "IP Address",
                "HTTP Referal",
                "Created By",
                "Creation Time"
        };

        Row headerRow = sheet.createRow(2);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 🔥 MUST for autoSize in streaming workbook
        sxssfSheet.trackAllColumnsForAutoSizing();

        /* ================= DATA ================= */

        int rowNum = 3;
        for (ActivityLogDTO entry : data) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            row.createCell(col++).setCellValue(entry.getActivityType());
            row.createCell(col++).setCellValue(entry.getMessage());
            row.createCell(col++).setCellValue(entry.getUserAgent());
            row.createCell(col++).setCellValue(entry.getIpAddress());
            row.createCell(col++).setCellValue(entry.getHttpReferal());
            row.createCell(col++).setCellValue(
                    entry.getCreatedBy() != null
                            ? entry.getCreatedBy().toString()
                            : ""
            );
            row.createCell(col++).setCellValue(entry.getActivityTime());
        }

        /* ================= AUTO SIZE ================= */

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);   // ✅ SAFE NOW
        }
    }
}
