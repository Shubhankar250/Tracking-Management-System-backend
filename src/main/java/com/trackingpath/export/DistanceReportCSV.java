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
import com.trackingpath.dtos.DistanceReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DistanceReportCSV")
public class DistanceReportCSV extends AbstractView {
	
	public DistanceReportCSV() {
        setContentType("text/csv");
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

        CSVWriter writer = new CSVWriter(response.getWriter());

        writer.writeNext(finalColumns.stream().map(headerMap::get).toArray(String[]::new));

        for (DistanceReportDto d : data) {
            List<String> row = new ArrayList<>();
            for (String col : finalColumns) {
                switch (col) {
                    case "device": row.add(d.getName()); break;
                    case "start": row.add(d.getStartPoint()); break;
                    case "end": row.add(d.getEndPoint()); break;
                    case "stime": row.add(d.getStime()); break;
                    case "etime": row.add(d.getEtime()); break;
                    case "distance": row.add(d.getDistance()); break;
                }
            }
            writer.writeNext(row.toArray(new String[0]));
        }

        writer.close();
    }

}
