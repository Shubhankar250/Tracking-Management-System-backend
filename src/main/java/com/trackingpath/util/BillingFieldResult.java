package com.trackingpath.util;

import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

public class BillingFieldResult {

    private int fieldNo;
    private int clusterId;
    private int totalPoints;
    private int segmentCount;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private double workedAreaSqm;
    private double workedAreaHectare;
    private double workedAreaAcre;

    private double clusterBoundaryAreaSqm;
    private double clusterBoundaryAreaHectare;
    private double clusterBoundaryAreaAcre;

    private Geometry workedGeometry;
    private Geometry clusterBoundaryGeometry;

    private String workedGeoJson;
    private String clusterBoundaryGeoJson;

    public int getFieldNo() {
        return fieldNo;
    }

    public void setFieldNo(int fieldNo) {
        this.fieldNo = fieldNo;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(int segmentCount) {
        this.segmentCount = segmentCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getWorkedAreaSqm() {
        return workedAreaSqm;
    }

    public void setWorkedAreaSqm(double workedAreaSqm) {
        this.workedAreaSqm = workedAreaSqm;
    }

    public double getWorkedAreaHectare() {
        return workedAreaHectare;
    }

    public void setWorkedAreaHectare(double workedAreaHectare) {
        this.workedAreaHectare = workedAreaHectare;
    }

    public double getWorkedAreaAcre() {
        return workedAreaAcre;
    }

    public void setWorkedAreaAcre(double workedAreaAcre) {
        this.workedAreaAcre = workedAreaAcre;
    }

    public double getClusterBoundaryAreaSqm() {
        return clusterBoundaryAreaSqm;
    }

    public void setClusterBoundaryAreaSqm(double clusterBoundaryAreaSqm) {
        this.clusterBoundaryAreaSqm = clusterBoundaryAreaSqm;
    }

    public double getClusterBoundaryAreaHectare() {
        return clusterBoundaryAreaHectare;
    }

    public void setClusterBoundaryAreaHectare(double clusterBoundaryAreaHectare) {
        this.clusterBoundaryAreaHectare = clusterBoundaryAreaHectare;
    }

    public double getClusterBoundaryAreaAcre() {
        return clusterBoundaryAreaAcre;
    }

    public void setClusterBoundaryAreaAcre(double clusterBoundaryAreaAcre) {
        this.clusterBoundaryAreaAcre = clusterBoundaryAreaAcre;
    }

    public Geometry getWorkedGeometry() {
        return workedGeometry;
    }

    public void setWorkedGeometry(Geometry workedGeometry) {
        this.workedGeometry = workedGeometry;
    }

    public Geometry getClusterBoundaryGeometry() {
        return clusterBoundaryGeometry;
    }

    public void setClusterBoundaryGeometry(Geometry clusterBoundaryGeometry) {
        this.clusterBoundaryGeometry = clusterBoundaryGeometry;
    }

    public String getWorkedGeoJson() {
        return workedGeoJson;
    }

    public void setWorkedGeoJson(String workedGeoJson) {
        this.workedGeoJson = workedGeoJson;
    }

    public String getClusterBoundaryGeoJson() {
        return clusterBoundaryGeoJson;
    }

    public void setClusterBoundaryGeoJson(String clusterBoundaryGeoJson) {
        this.clusterBoundaryGeoJson = clusterBoundaryGeoJson;
    }
}
