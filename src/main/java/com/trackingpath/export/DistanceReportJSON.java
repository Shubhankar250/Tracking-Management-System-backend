package com.trackingpath.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DistanceReportDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DistanceReportJSON")
public class DistanceReportJSON extends AbstractView {
	
	private static final Map<String, Function<DistanceReportDto, Object>> valueMap = new LinkedHashMap<>();

    static {
        valueMap.put("device", DistanceReportDto::getName);
        valueMap.put("start", DistanceReportDto::getStartPoint);
        valueMap.put("end", DistanceReportDto::getEndPoint);
        valueMap.put("stime", DistanceReportDto::getStime);
        valueMap.put("etime", DistanceReportDto::getEtime);
        valueMap.put("distance", DistanceReportDto::getDistance);
    }

    public DistanceReportJSON() {
        setContentType("application/json");
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

        List<String> allColumns = new ArrayList<>(valueMap.keySet());

        List<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? allColumns
                : allColumns.stream()
                .filter(col -> !selectedColumns.contains(col))
                    .toList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (DistanceReportDto d : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String col : finalColumns) {
                Object val = valueMap.get(col).apply(d);
                row.put(col, val != null ? val : "");
            }
            result.add(row);
        }

        new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(response.getWriter(), result);
    }

}
