package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LiveDataBean {

	
	 // Device info
    private long device_id;
    private String device_name;
    private String gps_status;

    // Group info
    private Long group_id;
    private String group_name;

    // Time info (already converted to user timezone)
    private LocalDateTime devicetime;
    private LocalDateTime servertime;

    // Time differences (human-readable)
    private String lastmovementtime;
    private String lastidletime;

    // Location
    private Double latitude;
    private Double longitude;
    private Double altitude;

    // Movement
    private Long speed;
    private Double course;

    // Address & raw attributes
    private String address;
    private String attributes;

    // Parsed attributes
    private boolean ignition;
    private boolean motion;
    private Double power;
    private Double battery;
    private Double distance;

    // Calculated status
    private String status; // MOVING / IDLE / STOPPED / NODATA
    private String uniqueid;
    // Device appearance settings
    private DeviceSettingCustomBean deviceSetting;
    private Long channelNo;
    // Resultant sensor data (post processing)
    private Object resultantSensorBean;
}
