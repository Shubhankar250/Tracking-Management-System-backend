package com.trackingpath.export;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.trackingpath.dtos.OverspeedReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("OverspeedReportPDF")
public class OverspeedReportPDF extends AbstractView {
	
	public OverspeedReportPDF() {
        setContentType("application/pdf");
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

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

        PdfDocument pdf = new PdfDocument(new PdfWriter(response.getOutputStream()));
        Document document = new Document(pdf);

     // ✅ Full width table
        Table table = new Table(finalColumns.size());
        table.setWidth(UnitValue.createPercentValue(100));

        // ✅ Bold font
        PdfFont bold = PdfFontFactory.createFont();

        // ================= HEADER =================
        for (String col : finalColumns) {
            table.addHeaderCell(
                new Cell()
                    .add(new Paragraph(headerMap.get(col)).setFont(bold))
                    .setBackgroundColor(new DeviceGray(0.85f))   // light gray
                    .setTextAlignment(TextAlignment.CENTER)      // center text
                    .setPadding(5)
            );
        }

        for (OverspeedReportDto d : data) {
            for (String col : finalColumns) {

                String val = switch (col) {
                    case "device" -> d.getDeviceName();
                    case "speed" -> String.valueOf(d.getSpeed());
                    case "speed_limit" -> String.valueOf(d.getSpeedLimit());
                    case "address" -> d.getAddress();
                    case "time" -> d.getDeviceTime();
                    default -> "";
                };

                // ✅ FIX: prevent null crash
                table.addCell(
                        new Cell()
                            .add(new Paragraph(val != null ? val : ""))
                            .setPadding(4)
                    );
            }
        }

        document.add(table);
        document.close();
    }

}
