package com.trackingpath.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoutePlannerResponse {
    private Long routeId;
    private Long shiftId;
    private String routeName;
    private String shiftName;
    private String startTime;
    private String endTime;
    private String routeType;
    private Long defaultVehicleId;
    private String sourceType;
    private String routeGeoJson;
    private List<String> activeDays = new ArrayList<>();
    private List<String> holidayDates = new ArrayList<>();
    private List<StopDto> stops = new ArrayList<>();
    private String defaultVehicleName;
    private List<PassengerAssignmentDto> passengers = new ArrayList<>();
}
