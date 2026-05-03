package com.trackingpath.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PassengerAssignmentDto {
    private Long id;
    private Long passengerId;
    private String passengerName;
    private String passengerType;
    private Long routeId;
    private Long pickupStopId;
    private String pickupStopName;
    private Long dropStopId;
    private String dropStopName;
    private String guardianName;
    private String guardianMobile;
    private Boolean autoLoginEnabled;
    private String username;
    private String tempPassword;
    private Boolean passwordChanged;
    private Boolean active;
    
}
