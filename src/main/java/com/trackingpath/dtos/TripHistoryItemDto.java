package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripHistoryItemDto {
    private String tripDate;
    private String routeType;
    private String attendanceStatus;
    private String boardingStatus;
    private String deboardingStatus;
    private String boardTime;
    private String dropTime;
    private String vehicleNumber;
    private String routeName;
}
