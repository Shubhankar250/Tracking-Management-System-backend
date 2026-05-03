package com.trackingpath.dtos;

public interface DeviceInfoProjection {

    String getIconType();
    String getImgIconName();
    String getName();
    String getStatus();

    Long getLastidletime();       // epoch seconds
    Long getLastmovementtime();   // epoch seconds
    
    String getDevicetimezone();
}