package com.trackingpath.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AlertSettingDTO {

	
	private Long id;

   // private Long id;

	@JsonProperty("alert_name")
	private String alertName;

	/*
	 * @JsonProperty("created_on") private String createdOn;
	 */

    private AlertDeviceMappingDTO alertDeviceMappingDTO;
    private AlertDetailsDTO alertDetailsDTO;
    private AlertGeofenceMappingDTO alertGeofenceMappingDTO;
    private AlertNotificationDTO alertNotificationDTO;
    private AlertScheduleDTO alertScheduleDTO;
    private AlertDeviceCommandDTO alertDeviceCommandDTO;
    private AlertUserDTO alertUserDTO;
    private AlertRouteMappingDTO alertRouteMappingDTO;
}

