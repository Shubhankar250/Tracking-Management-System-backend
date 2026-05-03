package com.trackingpath.dtos;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportScheduleDto {
	private Long id;
    private Long userId;
    private String scheduleName;
    private String reportType;
    private String emailTo;
    private String emailCc;
    private String outputFormat;
    private String scheduleType;
    private String cronExpression;
    private String timezone;
    private String filterJson;
    private LocalDateTime oneTimeRunAt;
    private Boolean active;
       // 🔥 NEW FIELDS
    private String from_date;
    private String to_date;
    private String title;
    private String period;
    private String speed_limit;
    private String stops;
    private String daily;
    private String weekly;
    private String monthly;
    private String subject;
    private String email_body;
    private List<String> skip_column;
    private List<Long> devices;
    private List<Long> geofences;
    private Long template_id ;
    private LocalDateTime createdAt;
    private String sheduleReportType;
    
    public ReportScheduleDto(
            Long id,
            String title,
            String reportType,
            String outputFormat,
            LocalDateTime createdAt,
            Boolean active,
            String daily,
            String weekly,
            String monthly
    ) {
        this.id = id;
        this.title = title;
        this.reportType = reportType;
        this.outputFormat = outputFormat;
        this.createdAt = createdAt;
        this.active = active;

        this.daily = daily;
        this.weekly = weekly;
        this.monthly = monthly;

        // 🔥 AUTO schedule type
        if (daily != null && !"false".equalsIgnoreCase(daily)) {
            this.sheduleReportType = "DAILY";
        } else if (weekly != null && !"false".equalsIgnoreCase(weekly)) {
            this.sheduleReportType = "WEEKLY";
        } else if (monthly != null && !"false".equalsIgnoreCase(monthly)) {
            this.sheduleReportType = "MONTHLY";
        } else {
            this.sheduleReportType = "GENERATED";
        }
    }
}
