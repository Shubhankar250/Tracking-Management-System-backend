package com.trackingpath.dtos;

import lombok.Data;

@Data
public class MaintenanceResponseDto {
	
	 private Long id;
	    private String serviceName;
	    private Long deviceId;
	    private String device_name;

	    private boolean datalist;
	    private boolean popup;

	    private boolean odometerIntervalKm;
	    private Long odometerIntervalKmVal;
	    private Long lastServiceKm;

	    private boolean engineHourInterval;
	    private Long engineHourIntervalVal;
	    private Long lastServiceHours;

	    private boolean daysInterval;
	    private Long daysIntervalVal;

	    private boolean odometerLeftKm;
	    private Long odometerLeftKmVal;

	    private boolean engineHoursLeft;
	    private Long engineHoursLeftVal;

	    private boolean updateLastService;

	    private boolean daysLeft;
	    private Long daysLeftVal;

	    private boolean eventTrigger;

	    private String lastServiceDate; // YYYY-MM-DD

	    private Long userId;
	    private Long adminId;
	    private String username;
	    private String admin_name;

}
