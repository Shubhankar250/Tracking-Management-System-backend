package com.trackingpath.export;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.trackingpath.dtos.EventDataBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("MovementReportPDF")
public class MovementReportPDF extends AbstractView {
	
	public MovementReportPDF() {
        setContentType("application/pdf");
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        List<EventDataBean> data =
                (List<EventDataBean>) model.get("MovementReport");

        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns");

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("name", "Name");
        headerMap.put("time", "Time");
        headerMap.put("latitude", "Latitude");
        headerMap.put("longitude", "Longitude");
        headerMap.put("speed", "Speed");
        headerMap.put("address", "Address");
        headerMap.put("distance", "Distance");

        Set<String> finalColumns;
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            finalColumns = new LinkedHashSet<>(headerMap.keySet());
        } else {
            finalColumns = headerMap.keySet().stream()
                    .filter(col -> !selectedColumns.contains(col))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Table table = new Table(finalColumns.size());
        table.setWidth(
        	    com.itextpdf.layout.properties.UnitValue.createPercentValue(100)
        	);

        PdfFont bold = PdfFontFactory.createFont();

        // ✅ HEADER (bold)
        for (String col : finalColumns) {
            table.addHeaderCell(
                new Cell()
                    .add(new Paragraph(headerMap.get(col)).setFont(bold))
                    .setBackgroundColor(new DeviceGray(0.85f))
            );
        }

        // ✅ DATA
        for (EventDataBean bean : data) {
            for (String col : finalColumns) {

                String val = "";

                switch (col) {
                    case "name": val = bean.getDetails().getName(); break;
                    case "time": val = bean.getDeviceTime(); break;
                    case "latitude": val = String.valueOf(bean.getLatitude()); break;
                    case "longitude": val = String.valueOf(bean.getLongitude()); break;
                    case "speed": val = String.valueOf(bean.getSpeed()); break;
                    case "address": val = bean.getAddress(); break;
                    case "distance": val = String.valueOf(bean.getDistance()); break;
                }

                table.addCell(new Cell().add(new Paragraph(val)));
            }
        }

        document.add(table);
        document.close();
    }

}
