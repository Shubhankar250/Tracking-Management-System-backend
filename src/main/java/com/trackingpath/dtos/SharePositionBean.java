package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SharePositionBean {

	private String accessStartTime;
	private String accessEndTime;

	private long id;
	private long accessCode;
	private long userId;
	private long adminId;

	private boolean status;

	private String deviceId;
	private String createdOn;
	private String baseUrl;
	private String deviceName;
	private String name;
	private String validTime;
	private String email;
	private String phone;
	private String devicetimezone;
	

	private boolean deleteAfterExpiration;

	private SharePositionSheduleBean sharePositionScheduleBean;

	public SharePositionBean(Long id, String deviceName, String accessStartTime, 
	        String accessEndTime, String deviceId, Long accessCode, 
	        String validTime, Boolean status, String baseUrl) {

	    this.id = id;
	    this.deviceName = deviceName;
	    this.accessStartTime = accessStartTime;
	    this.accessEndTime = accessEndTime;
	    this.deviceId = deviceId;
	    this.accessCode = accessCode;
	    this.validTime = validTime;
	    this.status = status;
	    this.baseUrl = baseUrl;
	}

}
