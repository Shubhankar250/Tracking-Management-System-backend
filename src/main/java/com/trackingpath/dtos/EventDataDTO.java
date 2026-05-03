package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDataDTO {

    private long id;
    private String protocol;
    private long deviceId;
    private String serverTime;
    private String deviceTime;
    private String fixTime;
    private String valid;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private double course;
    private String address;
    private String attributes;
    private double fuelLevel;
    private double distance;
    private int gsm;
    private boolean ignition;
    private int gps_satellite;
    private double battery;
    private DevicePlaybackDto details;

    private long flag;
    private long dataServerTime;
}