package com.trackingpath.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDetailsDTO {

    private Long id;           // add this field
    private String alertType;

    // numeric
    private Double overspeed;
    private Long stopDuration;
    private Long idleDuration;

    // match entity
    private Double lowspeed;

    // flags
    private String ignition;
    private Boolean sos;
    private Boolean vibration;
    private Boolean movement;
    private Boolean falldown;
    private Boolean lowpower;
    private Boolean lowbattery;
    private Boolean powercut;
    private Boolean powerrestored;

    private Long alertId;

    // driver
    private List<String> driverChangeIds;
    private Boolean driverChangeAuth;

    // POI
    private Integer poiStopDuration;
    private Integer poiIdleDuration;
    private List<String> poiIds;
    private List<String> adasEvents;
    private List<String> dmsEvents;
    
    private String adasDmsCategory;



}

