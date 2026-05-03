package com.trackingpath.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DailySummaryReportDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DailySummaryReportJSON")
public class DailySummaryReportJSON extends AbstractView {
	
	private static final Map<String, Function<DailySummaryReportDTO, Object>> valueMap = new LinkedHashMap<>();

    static {
        valueMap.put("date", DailySummaryReportDTO::getDate);
        valueMap.put("movement", DailySummaryReportDTO::getTotal_movement_time);
        valueMap.put("idle", DailySummaryReportDTO::getTotal_idle_time);
    }

    public DailySummaryReportJSON() {
        setContentType("application/json");
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

        List<String> allColumns = new ArrayList<>(valueMap.keySet());

        List<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? allColumns
                : allColumns.stream()
                .filter(col -> !selectedColumns.contains(col))
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (DailySummaryReportDTO d : data) {
            Map<String, Object> row = new LinkedHashMap<>();

            for (String col : finalColumns) {
                Object val = valueMap.get(col).apply(d);
                row.put(col, val != null ? val : "");
            }

            result.add(row);
        }

        new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValue(response.getWriter(), result);
    }

}
