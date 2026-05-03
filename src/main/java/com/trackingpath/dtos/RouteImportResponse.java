package com.trackingpath.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class RouteImportResponse {
    private String routeGeoJson;
    private List<GpsPointDto> rawGpsPoints = new ArrayList<>();
    private List<StopDto> stops = new ArrayList<>();
    private String sourceType;
    private String message;
}
