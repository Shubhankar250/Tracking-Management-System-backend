package com.trackingpath.dtos;

import lombok.Data;

@Data
public class TaskDTO {
    private Long id;
    private String name;
    private long object;
    private String priority;
    private String status;
    private String description;
    private String pickup_address;
    private String delivery_address;
    private String pickup_start_time;
    private String pickup_end_time;
    private String delivery_start_time;
    private String delivery_end_time;
    private String device_name;

    private String picked_up_time;
    private String delivered_time;

    private Double pickup_latitude;
    private Double pickup_longitude;

    private Double delivery_latitude;
    private Double delivery_longitude;

    private String task_image;
}