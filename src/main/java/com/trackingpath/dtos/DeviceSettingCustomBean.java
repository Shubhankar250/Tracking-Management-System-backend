package com.trackingpath.dtos;


import lombok.Data;

@Data
public class DeviceSettingCustomBean {

    private String icon_type;
    private String moving_icon_color;
    private String stopped_icon_color;
    private String offline_icon_color;
    private String engine_idle_color;
    private String img_icon_name;
    private String img_icon_type;
}
