package com.trackingpath.dtos;

public interface DriverView {

    Long getId();
    String getName();
    String getEmail();
    String getPhone();
    String getRfid();
    DeviceView getDevice();

    interface DeviceView {
        Long getId();
        String getName();
    }
}