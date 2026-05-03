package com.trackingpath.dtos;

import java.time.LocalDateTime;

public interface LiveDataProjection {

    Long getDeviceId();
    String getDeviceName();
    String getGpsStatus();
    String getStatus();
    String getUniqueid();
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

    String getIconType();
    String getMovingIconColor();
    String getStoppedIconColor();
    String getOfflineIconColor();
    String getEngineIdleColor();
    String getImgIconName();
    String getImgIconType();
    Long getChannelNo();
}
