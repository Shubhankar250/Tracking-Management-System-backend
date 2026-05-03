package com.trackingpath.dtos;

import java.util.ArrayList;
import java.util.List;

public class BillingSummary {

    private long deviceId;
    private double tractorWidthMeters;
    private int rawPoints;
    private int workingPoints;
    private int noisePoints;
    private int totalFields;

    private double totalWorkedAreaSqm;
    private double totalWorkedAreaHectare;
    private double totalWorkedAreaAcre;

    private final List<BillingFieldResult> fields = new ArrayList<>();

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public double getTractorWidthMeters() {
        return tractorWidthMeters;
    }

    public void setTractorWidthMeters(double tractorWidthMeters) {
        this.tractorWidthMeters = tractorWidthMeters;
    }

    public int getRawPoints() {
        return rawPoints;
    }

    public void setRawPoints(int rawPoints) {
        this.rawPoints = rawPoints;
    }

    public int getWorkingPoints() {
        return workingPoints;
    }

    public void setWorkingPoints(int workingPoints) {
        this.workingPoints = workingPoints;
    }

    public int getNoisePoints() {
        return noisePoints;
    }

    public void setNoisePoints(int noisePoints) {
        this.noisePoints = noisePoints;
    }

    public int getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(int totalFields) {
        this.totalFields = totalFields;
    }

    public double getTotalWorkedAreaSqm() {
        return totalWorkedAreaSqm;
    }

    public void setTotalWorkedAreaSqm(double totalWorkedAreaSqm) {
        this.totalWorkedAreaSqm = totalWorkedAreaSqm;
    }

    public double getTotalWorkedAreaHectare() {
        return totalWorkedAreaHectare;
    }

    public void setTotalWorkedAreaHectare(double totalWorkedAreaHectare) {
        this.totalWorkedAreaHectare = totalWorkedAreaHectare;
    }

    public double getTotalWorkedAreaAcre() {
        return totalWorkedAreaAcre;
    }

    public void setTotalWorkedAreaAcre(double totalWorkedAreaAcre) {
        this.totalWorkedAreaAcre = totalWorkedAreaAcre;
    }

    public List<BillingFieldResult> getFields() {
        return fields;
    }
}
