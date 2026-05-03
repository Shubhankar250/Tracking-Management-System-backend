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
import com.trackingpath.dtos.DailySummaryReportDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("DailySummaryReportCSV")
public class DailySummaryReportCSV extends AbstractView {
	
	public DailySummaryReportCSV() {
        setContentType("text/csv");
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

        CSVWriter writer = new CSVWriter(response.getWriter());

        writer.writeNext(finalColumns.stream().map(headerMap::get).toArray(String[]::new));

        for (DailySummaryReportDTO d : data) {
            List<String> row = new ArrayList<>();

            for (String col : finalColumns) {
                switch (col) {
                    case "date": row.add(d.getDate()); break;
                    case "movement": row.add(d.getTotal_movement_time()); break;
                    case "idle": row.add(d.getTotal_idle_time()); break;
                }
            }

            writer.writeNext(row.toArray(new String[0]));
        }

        writer.close();
    }

}
