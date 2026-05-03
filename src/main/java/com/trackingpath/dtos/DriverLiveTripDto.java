package com.trackingpath.dtos;

import java.util.List;

import com.trackingpath.dtos.LiveStopDto;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DriverLiveTripDto {

	private String routeName;
    private String routeGeoJson;

    private String currentStop;
    private String nextStop;
    private String nextStopEta;
    private String delay;
    private Long vehicleId;
    private String vehicleName;
    private Integer totalStudents;
    private Integer boarded;
    private Integer pending;
    private String scheduleArrivalTime;
    private String actualArrivalTime;
    

    private List<LiveStopDto> stops;
}
