package com.trackingpath.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDashboardResponse {
    private Long driverId;
    private String driverName;
    private String driverNumber;
    private boolean active;
    private String schoolName;
    private String helperName;
    private String routeName;
    private String totalPassengerAssign;
    private String vehicleName;
    private String tripTime;
    private String absentPassenger;
    private String presentPassenger;
    private String boardedCount;
    private String upcomingStop;
    private Double tripProgress;
    private Long totalStops;
    private Long coveredStops;
    private String depotDeparture;
    private String nextStop;
    private String lastStop; 
    private String startTime;
    private String expectedTime;
    private String shiftName;
   
}
