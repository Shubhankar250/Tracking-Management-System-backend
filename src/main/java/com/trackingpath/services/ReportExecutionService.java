package com.trackingpath.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.ReportScheduleLog;
import com.trackingpath.repositories.ReportScheduleLogRepository;
import com.trackingpath.repositories.ReportScheduleRepository;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class ReportExecutionService {

    @Autowired
    private ReportGeneratorFactory reportGeneratorFactory;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReportScheduleLogRepository logRepository;

    @Autowired
    private ReportScheduleRepository scheduleRepository;

    public void executeSchedule(ReportSchedule schedule) {
        ReportScheduleLog log = new ReportScheduleLog();
        log.setScheduleId(schedule.getId());
        log.setStartedAt(LocalDateTime.now());
        log.setStatus("FAILED");

        try {
            File file = reportGeneratorFactory
                    .getGenerator(schedule.getReportType())
                    .generate(schedule);
            long fileSize = file.length();
            double kb = fileSize / 1024.0;
            double mb = kb / 1024.0;

            String readable = (mb >= 1)
                    ? String.format("%.2f MB", mb)
                    : String.format("%.2f KB", kb);
            log.setFileSize(readable);
            emailService.sendReportEmail(schedule, file);

            log.setCompletedAt(LocalDateTime.now());
            log.setStatus("SUCCESS");
            log.setFilePath(file.getAbsolutePath());
            log.setRecipientCount(schedule.getEmailTo().split("\\s*,\\s*").length);

            schedule.setLastRunAt(LocalDateTime.now());
            scheduleRepository.save(schedule);

        } catch (Exception e) {
        	e.printStackTrace();
            log.setCompletedAt(LocalDateTime.now());
            log.setErrorMessage(e.getMessage());
        }

        logRepository.save(log);
    }
}
