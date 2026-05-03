package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildTripStatusResponse {
    private Long tripId;
    private String tripDate;
    private String routeType;
    private String attendanceStatus;
    private String boardingStatus;
    private String deboardingStatus;
    private String pickupStopName;
    private String dropStopName;
    private String markedBy;
    private String markedTime;
    private String remarks;
}
