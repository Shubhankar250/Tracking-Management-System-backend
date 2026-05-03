package com.trackingpath.services;


import java.io.File;

import com.trackingpath.entities.ReportSchedule;

public interface ReportGenerator {
    File generate(ReportSchedule schedule) throws Exception;
}
