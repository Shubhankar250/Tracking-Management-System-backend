package com.trackingpath.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.MaintenanceEntity;

import lombok.Data;

@Data
public class LiveDataDto {

	private String device_name;
	private Long  device_id,group_id;
	private String group_name;
	private LocalDateTime devicetime;
	private LocalDateTime servertime;
	private String lastidletime;
	private String lastmovementtime;	
	private double course;
	private String attributes;
	private String vehicle_status;
	private String status;
	private double latitude;
	private double longitude;
	private double altitude;
	private double speed;
	private String address;
	private String last_command;
	
	private long satelite;
	private boolean ac;
	private double fuel;
	private double power;
	private String engine_status;
	private String parking;
	/*
	 * private DeviceSettingCustomBean deviceSetting; private
	 * List<ResultantSensorBean> resultantSensorBean;
	 */
	private double battery;
	private double distance;
	private boolean motion;
	 private String tail;
	 private int tail_width;
	 private int min_moving_speed;
	private List<AlertDTO> events;
	
	 private String service_name,last_service_date;
	  private long last_service_km;
	  private List<MaintenanceView> services;
      private String filter_status;

      private String objectIcon;
     private List<DriverView> drivers;
     private DeviceSettingCustomBean deviceSetting;
     private String uniqueid;
 	private String simCardNumber;
	private String deviceModel;
	private String modalType;
	private String devicetimezone;
}
