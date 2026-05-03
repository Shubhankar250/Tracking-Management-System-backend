package com.trackingpath.scheduler;


import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.ReportSchedule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class QuartzReportSchedulerService {

    @Autowired
    private Scheduler scheduler;

    public void scheduleJob(ReportSchedule schedule) throws SchedulerException {
    	  System.out.println("in job scheduler");
        JobKey jobKey = JobKey.jobKey("report-job-" + schedule.getId(), "report-jobs");
        TriggerKey triggerKey = TriggerKey.triggerKey("report-trigger-" + schedule.getId(), "report-triggers");

        JobDetail jobDetail = newJob(ReportEmailJob.class)
                .withIdentity(jobKey)
                .usingJobData("scheduleId", schedule.getId())
                .storeDurably()
                .build();

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        Trigger trigger;

        if ("CRON".equalsIgnoreCase(schedule.getScheduleType())) {
            trigger = newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withSchedule(
                            cronSchedule(schedule.getCronExpression())
                            .inTimeZone(TimeZone.getTimeZone("UTC")) 
                                    .withMisfireHandlingInstructionDoNothing()
                    )
                    .build();

        } else if ("ONCE".equalsIgnoreCase(schedule.getScheduleType())) {

            // ✅ Direct UTC time lo (server time)
            Instant runTime = Instant.now().plusSeconds(3);

            Date startAt = Date.from(runTime);

            trigger = newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .startAt(startAt)
                    .withSchedule(
                            simpleSchedule()
                                    .withMisfireHandlingInstructionFireNow()
                    )
                    .build();
        }        
         else {
            throw new IllegalArgumentException("Unsupported schedule type: " + schedule.getScheduleType());
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(Long scheduleId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("report-job-" + scheduleId, "report-jobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }
}
