package com.trackingpath.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReportScheduleLogDto {

    private Long logId;
    private Long scheduleId;

    private String title;
    private String reportType;
    private String outputFormat;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String status;
    private Integer recipientCount;

    private String filePath;
    private String errorMessage;
    private String fileSize;
    private String daily;
    private String weekly;
    private String monthly;
    private String sheduleReportType;
}