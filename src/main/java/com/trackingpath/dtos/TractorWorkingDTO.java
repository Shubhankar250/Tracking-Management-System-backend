package com.trackingpath.dtos;

import java.util.List;

import lombok.Data;

@Data
public class TractorWorkingDTO {

    private int fieldNo;

    private String startTime;
    private String endTime;

    private double workedArea;
    private double fieldArea;
    private double coverage;

    private List<LatLng> boundaryPoints;
    private List<LatLng> workedPoints;

}
