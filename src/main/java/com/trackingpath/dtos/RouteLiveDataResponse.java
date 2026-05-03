package com.trackingpath.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
@Data
public class RouteLiveDataResponse {

	private String address;
	private double latitude;
	private double longitude;
	private long speed;
	private String nextlanmark;
	private LocalDateTime schooleta;
	private String vehiclename;
    private double pickupLatitude;
    private double pickupLongitude;

    private double dropLatitude;
    private double dropLongitude;

    private String dropStopName;
    private String pickupStopName;
	private List<SchoolNotificationDTO> notification;
}
