package com.trackingpath.dtos;

import lombok.Data;

@Data
public class SettingDTO {

    private String serverName;
    private String serverDescription;
    private String defaultLanguage;
    private String defaultDateFormat;
    private String defaultTimeFormat;
    private String defaultDurationFormat;
    private String defaultUnitOfDistance;
    private String defaultUnitOfCapacity;
    private String defaultUnitOfAltitude;
    private String mapZoomLevel;
    private String latitude;
    private String longitude;
    private String noReplyEmailAddress;
    private String fromName;
    private SettingLogoDTO logo;


  
  
}
