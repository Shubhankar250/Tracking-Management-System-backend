package com.trackingpath.scheduler;



import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.services.ReportExecutionService;
import com.trackingpath.services.ReportScheduleService;

@Component
public class ReportEmailJob implements Job {

    @Autowired
    private ReportScheduleService reportScheduleService;

    @Autowired
    private ReportExecutionService reportExecutionService;

    @Override
    public void execute(JobExecutionContext context) {
        Long scheduleId = context.getMergedJobDataMap().getLong("scheduleId");

        ReportSchedule schedule = reportScheduleService.getActiveSchedule(scheduleId);
        if (schedule == null) {
            return;
        }

        reportExecutionService.executeSchedule(schedule);
        
     // 🔥 IMPORTANT: ONCE job deactivated
        if ("ONCE".equalsIgnoreCase(schedule.getScheduleType())) {
            reportScheduleService.deactivateSchedule(scheduleId);
        }
    }
}
