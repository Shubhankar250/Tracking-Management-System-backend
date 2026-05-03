package com.trackingpath.export;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.trackingpath.dtos.DistanceReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DistanceReportHTML")
public class DistanceReportHTML extends AbstractView {
	
	public DistanceReportHTML() {
        setContentType("text/html");
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

        PrintWriter writer = response.getWriter();

        writer.write("<html><body><table border='1'>");

        writer.write("<tr>");
        for (String col : finalColumns) {
            writer.write("<th>" + headerMap.get(col) + "</th>");
        }
        writer.write("</tr>");

        for (DistanceReportDto d : data) {
            writer.write("<tr>");
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
                writer.write("<td>" + (val != null ? val : "") + "</td>");
            }
            writer.write("</tr>");
        }

        writer.write("</table></body></html>");
    }

}
