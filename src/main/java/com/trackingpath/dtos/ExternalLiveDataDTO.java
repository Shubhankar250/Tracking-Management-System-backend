package com.trackingpath.dtos;

import lombok.Data;

@Data
public class ExternalLiveDataDTO {

	  private String vehicle_name;
	  
	  private double speed;
	  
	  private String deviceTime;
	  
	  private String driver_name;
	  
	  private String address;
	    private String deviceId;

	  private double latitude;
	  
	  private double longitude;
	  private String status;
	  
	
	   public ExternalLiveDataDTO(double latitude, double longitude, String vehicle_name,
               String deviceId, double speed, String address, String deviceTime,String status) {
this.latitude = latitude;
this.longitude = longitude;
this.vehicle_name = vehicle_name;
this.deviceId = deviceId;
this.speed = speed;
this.address = address;
this.deviceTime = deviceTime;
this.status=status;
}
	   
}
