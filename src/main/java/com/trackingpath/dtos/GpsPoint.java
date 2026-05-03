package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class GpsPoint {

    private long deviceId;
    private double latitude;
    private double longitude;
    private double speed;
    private LocalDateTime deviceTime;

    public GpsPoint(long deviceId, double latitude, double longitude, double speed, LocalDateTime deviceTime) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.deviceTime = deviceTime;
    }

	

}
