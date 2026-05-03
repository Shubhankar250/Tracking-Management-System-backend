package com.trackingpath.dtos;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;


@Data
public class BillingFieldResult {

    private int fieldNo;
    private int clusterId;
    private int totalPoints;
    private int segmentCount;

    private String startTime;
    private String endTime;

    private double workedAreaSqm;
    private double workedAreaHectare;
    private double workedAreaAcre;

    private double clusterBoundaryAreaSqm;
    private double clusterBoundaryAreaHectare;
    private double clusterBoundaryAreaAcre;
    @JsonIgnore
    private Geometry workedGeometry;
    @JsonIgnore
    private Geometry clusterBoundaryGeometry;

    private String workedGeoJson;
    private String clusterBoundaryGeoJson;

   
}
