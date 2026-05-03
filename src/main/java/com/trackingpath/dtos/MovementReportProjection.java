package com.trackingpath.dtos;
public interface MovementReportProjection {

    Long getDeviceid();
    String getName();
    String getDevice_model();

    String getAddress();
    Double getAltitude();
    String getAttributes();
    Double getCourse();
    Double getLatitude();
    Double getLongitude();
    Double getSpeed();
    Boolean getValid();
    Double getFuellevel();

    Long getDevicetime();
    Long getFixtime();
    Long getServertime();
}
