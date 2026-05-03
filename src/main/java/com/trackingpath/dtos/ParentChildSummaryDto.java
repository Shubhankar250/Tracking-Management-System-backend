package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentChildSummaryDto {
    private Long passengerId;
    private String passengerName;
    private String className;
    private String routeName;
    private String pickupStop;
    private String tripStatus;
    private String boardingStatus;
    private String vehicleName;
    private String driverName;
    private LocalDateTime boardTime;
    private LocalDateTime ETA;
    private String shiftName;
    
}
