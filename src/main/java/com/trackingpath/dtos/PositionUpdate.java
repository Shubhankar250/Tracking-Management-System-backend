package com.trackingpath.dtos;

import java.util.Map;

import lombok.Data;

@Data
public class PositionUpdate {

    private long deviceId;
    private String deviceUniqueId;

    private double latitude;
    private double longitude;
    private double speed;
    private double course;
    private boolean valid;

    private long fixTime;       // epoch millis
    private Long devicetime;    // epoch millis

    private Long servertime; 
    private Boolean ignition;

    private String lastidletime;
    private String lastmovementtime;
  
    private double altitude;
    private String address;

    private Map<String, Object> attributes;

    private String status;
    private String imgIconName;
    private String iconType;
    private String name;
    private String devicetimezone;
      
}
