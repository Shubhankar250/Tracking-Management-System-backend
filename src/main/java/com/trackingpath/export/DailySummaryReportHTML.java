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

import com.trackingpath.dtos.DailySummaryReportDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DailySummaryReportHTML")
public class DailySummaryReportHTML extends AbstractView {
	
	public DailySummaryReportHTML() {
        setContentType("text/html");
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

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

        PrintWriter writer = response.getWriter();

        writer.write("<html><body><table border='1'>");

        writer.write("<tr>");
        for (String col : finalColumns) {
            writer.write("<th>" + headerMap.get(col) + "</th>");
        }
        writer.write("</tr>");

        for (DailySummaryReportDTO d : data) {
            writer.write("<tr>");

            for (String col : finalColumns) {
                String val = switch (col) {
                    case "date" -> d.getDate();
                    case "movement" -> d.getTotal_movement_time();
                    case "idle" -> d.getTotal_idle_time();
                    default -> "";
                };

                writer.write("<td>" + (val != null ? val : "") + "</td>");
            }

            writer.write("</tr>");
        }

        writer.write("</table></body></html>");
    }

}
