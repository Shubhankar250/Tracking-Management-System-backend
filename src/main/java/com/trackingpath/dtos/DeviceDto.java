package com.trackingpath.dtos;

import lombok.Data;

@Data
public class DeviceDto {

    private String name;
    private String uniqueId;
    private String model;
    private String vin;
    private String plateNumber;
    private Long groupId;
    private Long driverId;
    private Long helperId;
    private String trailer;
    private String simCardNumber;
    private String simImsiNumber;
    private Double odometer;
    private Double engineHours;
    private String address;

    // getters & setters
}
