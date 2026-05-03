package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BreakdownDetailsDto {

    private String routeName;

    private String breakdownStopName;
    private String breakdownTime;

    private Integer boardedStudentCount;

    private String estimatedSupportTimeMinutes;

    // After replacement
    private String replacementDeviceName;
    private String replacementDriverName;
    private String etaMinutes;
}