package com.trackingpath.dtos;

import java.time.LocalDateTime;

public interface LiveDataView {

    Long getDeviceId();
    String getDeviceName();
    String getObjectIcon();
    String getStatus();
    String getVehicleStatus();

    Long getGroupId();
    String getGroupName();
    LocalDateTime getDeviceTime();
    LocalDateTime getServerTime();
    LocalDateTime getLastIdleTime();
    LocalDateTime getLastMovementTime();

    Double getLatitude();
    Double getLongitude();
    Double getAltitude();
    Double getSpeed();
    Double getCourse();

    String getAddress();
    String getAttributes();

    String getImgIconName();
    String getImgIconType();
    String getIconType();

    String getMovingIconColor();
    String getStoppedIconColor();
    String getOfflineIconColor();
    String getEngineIdleColor();

    Integer getTailLength();
    String getTailColor();
    Integer getMinMovingSpeed();
    String getUniqueid();
    String getSimCardNumber();
    String getDeviceModel();
    String getModalType();
    String getDeviceTimezone();
}
