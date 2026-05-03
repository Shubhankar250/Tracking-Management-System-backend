package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryReportDTO {
	
	private long deviceId;
	private long total_records;
	private double total_distance;
	private long total_ignition_on_time;
	private long total_overspeed;
	private String date;
	private double maximum_speed;
	private double minimum_speed;
	private double average_speed;
	private double fuel_consumption;
	private String device_name;
	
	private String total_idle_time;
	private String total_movement_time;

}
