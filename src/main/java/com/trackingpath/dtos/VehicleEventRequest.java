package com.trackingpath.dtos;

import lombok.Data;

@Data
public class VehicleEventRequest {

    private Long driverId;

    private String eventType; 

    private Long newVehicleId; 

    private String reason;
    private String remarks;
}