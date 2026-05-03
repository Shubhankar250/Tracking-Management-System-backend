package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationDTO {

    private Long deviceId;
    private Long userId;

    private String deviceName;
    private String alertType;
    private String message;
    private String address;

    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double course;
    private Double altitude;

    private String attributes;
    private String status;
    private LocalDateTime alertTime;

    private String popupNotification;
    private String soundNotification;
    private String notificationColor;

    
    
	
	
	
	
	
}

