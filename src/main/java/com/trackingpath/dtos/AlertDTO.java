package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AlertDTO {
	

	
	private String alert_type;
	private LocalDateTime alert_time;
	private Double speed;
}
