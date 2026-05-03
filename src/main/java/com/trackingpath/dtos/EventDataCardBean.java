package com.trackingpath.dtos;

import lombok.Data;

@Data
public class EventDataCardBean {
    private String event_name;
    private String event_time;
    private double latitude;
    private double longitude;
    private String address;
    private double speed;
}