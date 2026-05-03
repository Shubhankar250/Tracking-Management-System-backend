package com.trackingpath.dtos;

import lombok.Data;

@Data
public class ParkingDataBean {

    private long id;
    private String name;
    private String address;
    private String time;
    private double lat;
    private double lon;

    private String parking_start_time;
    private String parking_end_time;
    private long epoch_idle_time;
}