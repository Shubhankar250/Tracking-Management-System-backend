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

import com.trackingpath.dtos.OverspeedReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("OverspeedReportHTML")
public class OverspeedReportHTML extends AbstractView {
	
	public OverspeedReportHTML() {
        setContentType("text/html");
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

        PrintWriter writer = response.getWriter();

        writer.write("<html><body><table border='1'>");

        writer.write("<tr>");
        for (String col : finalColumns) {
            writer.write("<th>" + headerMap.get(col) + "</th>");
        }
        writer.write("</tr>");

        for (OverspeedReportDto d : data) {
            writer.write("<tr>");
            for (String col : finalColumns) {
                String val = switch (col) {
                    case "device" -> d.getDeviceName();
                    case "speed" -> String.valueOf(d.getSpeed());
                    case "speed_limit" -> String.valueOf(d.getSpeedLimit());
                    case "address" -> d.getAddress();
                    case "time" -> d.getDeviceTime();
                    default -> "";
                };
                writer.write("<td>" + val + "</td>");
            }
            writer.write("</tr>");
        }

        writer.write("</table></body></html>");
    }

}
