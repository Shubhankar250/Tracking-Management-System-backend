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
import com.trackingpath.dtos.DistanceReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DistanceReportPDF")
public class DistanceReportPDF extends AbstractView {
	
	public DistanceReportPDF() {
        setContentType("application/pdf");
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

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

        PdfDocument pdf = new PdfDocument(new PdfWriter(response.getOutputStream()));
        Document document = new Document(pdf);

        Table table = new Table(finalColumns.size());
        table.setWidth(UnitValue.createPercentValue(100));

        PdfFont bold = PdfFontFactory.createFont();

        for (String col : finalColumns) {
            table.addHeaderCell(
                new Cell()
                    .add(new Paragraph(headerMap.get(col)).setFont(bold))
                    .setBackgroundColor(new DeviceGray(0.85f))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5)
            );
        }

        for (DistanceReportDto d : data) {
            for (String col : finalColumns) {
                String val = switch (col) {
                    case "device" -> d.getName();
                    case "start" -> d.getStartPoint();
                    case "end" -> d.getEndPoint();
                    case "stime" -> d.getStime();
                    case "etime" -> d.getEtime();
                    case "distance" -> d.getDistance();
                    default -> "";
                };

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
