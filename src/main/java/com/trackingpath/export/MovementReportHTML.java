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

import com.trackingpath.dtos.EventDataBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("MovementReportHTML")
public class MovementReportHTML extends AbstractView{
	
	public MovementReportHTML() {
        setContentType("text/html");
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

            if (finalColumns.isEmpty()) {
                finalColumns = new LinkedHashSet<>(headerMap.keySet());
            }
        }

        PrintWriter writer = response.getWriter();

        writer.write("<html><body><table border='1'>");

        // ✅ HEADER (bold automatically in <th>)
        writer.write("<tr>");
        for (String col : finalColumns) {
            writer.write("<th>" + headerMap.get(col) + "</th>");
        }
        writer.write("</tr>");

        // ✅ DATA
        for (EventDataBean bean : data) {
            writer.write("<tr>");
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
                writer.write("<td>" + val + "</td>");
            }
            writer.write("</tr>");
        }

        writer.write("</table></body></html>");
    }

}
