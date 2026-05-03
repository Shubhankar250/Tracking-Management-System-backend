package com.trackingpath.dtos;

import lombok.Data;

@Data
public class StopDto {
    private Long id;
    private Integer sequenceNo;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private String stopType;
    private Integer geofenceRadius;
    private Boolean autoDetected;
    private Boolean approved;
    private Integer passengerCount;
    private Long clientStopId;
    private String announcementFile;
}
