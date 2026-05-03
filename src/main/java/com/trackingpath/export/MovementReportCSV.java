package com.trackingpath.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.opencsv.CSVWriter;
import com.trackingpath.dtos.EventDataBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("MovementReportCSV")
public class MovementReportCSV extends AbstractView {

    public MovementReportCSV() {
        setContentType("text/csv");
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

        CSVWriter writer = new CSVWriter(response.getWriter());

        writer.writeNext(
                finalColumns.stream()
                        .map(headerMap::get)
                        .toArray(String[]::new)
        );

        for (EventDataBean bean : data) {
            List<String> row = new ArrayList<>();
            for (String col : finalColumns) {
                switch (col) {
                    case "name":
                        row.add(bean.getDetails().getName());
                        break;
                    case "time":
                        row.add(bean.getDeviceTime());
                        break;
                    case "latitude":
                        row.add(String.valueOf(bean.getLatitude()));
                        break;
                    case "longitude":
                        row.add(String.valueOf(bean.getLongitude()));
                        break;
                    case "speed":
                        row.add(String.valueOf(bean.getSpeed()));
                        break;
                    case "address":
                        row.add(bean.getAddress());
                        break;
                    case "distance":
                        row.add(String.valueOf(bean.getDistance()));
                        break;
                }
            }
            writer.writeNext(row.toArray(new String[0]));
        }

        writer.close();
    }
}
