package com.trackingpath.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.EventDataBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("MovementReportJSON")
public class MovementReportJSON extends AbstractView {

    // ✅ CENTRALIZED VALUE MAP (NO SWITCH CASE NEEDED)
    private static final Map<String, java.util.function.Function<EventDataBean, Object>> valueMap = new LinkedHashMap<>();

    static {
        valueMap.put("name", b -> b.getDetails().getName());
        valueMap.put("time", EventDataBean::getDeviceTime);
        valueMap.put("latitude", EventDataBean::getLatitude);
        valueMap.put("longitude", EventDataBean::getLongitude);
        valueMap.put("speed", EventDataBean::getSpeed);
        valueMap.put("address", EventDataBean::getAddress);
        valueMap.put("distance", EventDataBean::getDistance);
    }

    public MovementReportJSON() {
        setContentType("application/json");
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        List<EventDataBean> data =
                (List<EventDataBean>) model.get("MovementReport");

        List<String> selectedColumns =
                (List<String>) model.get("selectedColumns"); // 🔥 columns to SKIP

        List<Map<String, Object>> result = new ArrayList<>();

        // ✅ ALL columns
        List<String> allColumns = new ArrayList<>(valueMap.keySet());

        // ✅ REMOVE selected (skip logic)
        List<String> finalColumns = (selectedColumns == null || selectedColumns.isEmpty())
                ? allColumns
                : allColumns.stream()
                    .filter(col -> !selectedColumns.contains(col))
                    .toList();

        // ✅ DATA LOOP
        for (EventDataBean bean : data) {

            Map<String, Object> row = new LinkedHashMap<>();

            for (String col : finalColumns) {
                java.util.function.Function<EventDataBean, Object> fn = valueMap.get(col);
                Object val = (fn != null) ? fn.apply(bean) : "";
                row.put(col, val != null ? val : "");
            }

            result.add(row);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(response.getWriter(), result);
    }
}