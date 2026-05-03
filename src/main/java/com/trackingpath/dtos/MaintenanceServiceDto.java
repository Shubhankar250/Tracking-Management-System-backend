package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceServiceDto {

	private Long id;
	private Long deviceId;
	private String deviceName;
	private String serviceName;

	private Long odometerIntervalKmVal;
	private Long odometerLeftKmVal;

	private Long engineHourIntervalVal;
	private Long engineHoursLeftVal;

	private Long daysIntervalVal;
	private Long daysLeftVal;

	private Boolean eventTrigger;
}
