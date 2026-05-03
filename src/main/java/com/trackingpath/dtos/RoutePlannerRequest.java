package com.trackingpath.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoutePlannerRequest {
    private Long routeId;
    private String shiftName;
    private String startTime;
    private String endTime;
    private List<String> activeDays = new ArrayList<>();
    private List<LocalDate> holidayDates = new ArrayList<>();
    private String routeName;
    private String routeType;
    private Long defaultVehicleId;
    
    private String sourceType;
    private String routeGeoJson;
    private List<StopDto> stops = new ArrayList<>();
    private List<PassengerAssignmentDto> passengers = new ArrayList<>();

}
