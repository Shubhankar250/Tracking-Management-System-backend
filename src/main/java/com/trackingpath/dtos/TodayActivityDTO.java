package com.trackingpath.dtos;

import lombok.Data;

@Data
public class TodayActivityDTO {

    private String totalRunningTime;

    private String totalIdleTime;

    private String totalStopTime;

    private String workingHours;

    private String workStartTime;

    private String workEndTime;
    private double totalDistance;
}