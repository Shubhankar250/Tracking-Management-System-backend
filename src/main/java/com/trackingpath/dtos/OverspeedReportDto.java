package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverspeedReportDto {

    private double speed;
    private String deviceName;
    private String deviceModel;
    private String address;
    private String deviceTime;
    private String serverTime;
    private double latitude;
    private double longitude;
    private long speedLimit;

}