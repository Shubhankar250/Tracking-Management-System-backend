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
import com.trackingpath.dtos.OverspeedReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("OverspeedReportCSV")
public class OverspeedReportCSV extends AbstractView {
	
	public OverspeedReportCSV() {
        setContentType("text/csv");
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

        CSVWriter writer = new CSVWriter(response.getWriter());

        writer.writeNext(finalColumns.stream().map(headerMap::get).toArray(String[]::new));

        for (OverspeedReportDto d : data) {
            List<String> row = new ArrayList<>();
            for (String col : finalColumns) {
                switch (col) {
                    case "device": row.add(d.getDeviceName()); break;
                    case "speed": row.add(String.valueOf(d.getSpeed())); break;
                    case "speed_limit": row.add(String.valueOf(d.getSpeedLimit())); break;
                    case "address": row.add(d.getAddress()); break;
                    case "time": row.add(d.getDeviceTime()); break;
                }
            }
            writer.writeNext(row.toArray(new String[0]));
        }

        writer.close();
    }

}
