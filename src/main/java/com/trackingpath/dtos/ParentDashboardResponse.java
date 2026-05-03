package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDashboardResponse {
    private Long passengerId;
    private String passengerName;
    private String className;
    private String sectionName;
    private String routeName;
    private String pickupStop;
    private String vehicleNumber;
    private String tripStatus;
    private String boardingStatus;
    private String attendanceStatus;
    private String boardedTime;
    private String schoolEta;
    private Double distanceKm;
    private Integer notificationCount;
}
