package com.trackingpath.export;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import com.opencsv.CSVWriter;
import com.trackingpath.dtos.ActivityLogDTO;

public class ActivityLogCSV extends AbstractView {

    public ActivityLogCSV() {
        setContentType("text/csv");  // <-- tell browser this is CSV
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<ActivityLogDTO> data = (List<ActivityLogDTO>) model.get("ActivityLogDataCSV");

        response.setCharacterEncoding("UTF-8"); // optional, ensures correct encoding

        CSVWriter writer = new CSVWriter(response.getWriter());

        // CSV Header
        String[] headers = {"Log Type","Message","User Agent","IP Address","HTTP Referal","Created By","Creation Time"};
        writer.writeNext(headers);

        // CSV Rows
        for (ActivityLogDTO bean : data) {
            writer.writeNext(new String[]{
                    bean.getActivityType(),
                    bean.getMessage(),
                    bean.getUserAgent(),
                    bean.getIpAddress(),
                    bean.getHttpReferal(),
                    String.valueOf(bean.getCreatedBy()),
                    bean.getActivityTime()
            });
        }

        writer.close();     
    }
}
