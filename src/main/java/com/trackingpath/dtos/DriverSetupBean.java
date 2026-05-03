package com.trackingpath.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriverSetupBean {

	private Long id;
	private String name;
	private Long deviceId;
	private String deviceName;
	private Long currentDeviceId;
	private String rfid;
	private String phone;
	private String email;
	private String description;
	private String username;
	private String password;
 
	public DriverSetupBean(Long id, String name, Long deviceId, String deviceName, Long currentDeviceId, String rfid,
			String phone, String email, String description,String usrename) {
		this.id = id;
		this.name = name;
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.currentDeviceId = currentDeviceId;
		this.rfid = rfid;
		this.phone = phone;
		this.email = email;
		this.description = description;
		this.username=usrename;
	}
}
