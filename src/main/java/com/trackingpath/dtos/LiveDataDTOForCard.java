package com.trackingpath.dtos;


import lombok.Data;

@Data
public class LiveDataDTOForCard {

    private String total_running_time;
    private String idle_time;
    private double max_speed;
    private double average_speed;
}