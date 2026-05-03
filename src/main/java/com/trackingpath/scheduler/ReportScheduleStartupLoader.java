package com.trackingpath.scheduler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.repositories.ReportScheduleRepository;

@Component
public class ReportScheduleStartupLoader {

    @Autowired
    private ReportScheduleRepository repository;

    @Autowired
    private QuartzReportSchedulerService quartzReportSchedulerService;

    @PostConstruct
    public void loadSchedules() {
        for (ReportSchedule schedule : repository.findAll()) {
            try {
                if (Boolean.TRUE.equals(schedule.getActive())) {
                    quartzReportSchedulerService.scheduleJob(schedule);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
