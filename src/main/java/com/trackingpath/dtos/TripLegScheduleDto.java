package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripLegScheduleDto {
    private String routeName;
    private String plannedDepartureFromDepot;
    private String plannedStopArrival;
    private String actualBoardingTime;
    private String plannedSchoolArrival;
    private String plannedSchoolDeparture;
    private Boolean guardianHandoverRequired;
    private String backupGuardianName;
}
