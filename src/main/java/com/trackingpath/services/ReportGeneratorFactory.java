package com.trackingpath.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReportGeneratorFactory {

    @Autowired
    private Map<String, ReportGenerator> generators;

    public ReportGenerator getGenerator(String reportType) {
        ReportGenerator generator = generators.get(reportType);
        if (generator == null) {
            throw new IllegalArgumentException("No generator found for report type: " + reportType);
        }
        return generator;
    }
}
