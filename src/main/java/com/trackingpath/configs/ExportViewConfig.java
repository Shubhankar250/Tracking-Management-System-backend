package com.trackingpath.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.trackingpath.export.ActivityLogCSV;
import com.trackingpath.export.ActivityLogExcel;

@Configuration
public class ExportViewConfig {

    @Bean(name = "ActivityLogCSV")
    public ActivityLogCSV activityLogCSV() {
        return new ActivityLogCSV();
    }

    @Bean(name = "ActivityLogExcel")
    public ActivityLogExcel activityLogExcel() {
        return new ActivityLogExcel();
    }
}
