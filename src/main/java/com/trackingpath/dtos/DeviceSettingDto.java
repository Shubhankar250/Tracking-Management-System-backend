package com.trackingpath.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DeviceSettingDto {

	private String name;
	private String uniqueid;

	private String simCardNumber;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime simActivationDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime simExpirationDate;
	private String vin;
	private String deviceModel;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime installationDate;
	private String plateNumber;
	private String registrationNumber;
	private String owner;
	private String fuelMeasureName;
	private BigDecimal fuelMeasurement;
	private BigDecimal fuelCost;
	private String iconType;
	private String movingIconColor;
	private String stoppedIconColor;
	private String offlineIconColor;
	private String engineIdleColor;
	private String status;
	private String sensors;
	private String tailColor;
	private Integer tailLength;
	private String imgIconName;
	private String imgIconType;
	private Double odometer;
	private String vehicleStatus;
	private String maxSpeed;
	private String minMovingSpeed;
	private String minFuelFillings;
	private String minFuelTheft;
	private String fuelChangeAfterStop;
	private String objectIcon;
	private String deviceTimezone;

	private Long groupId;
	private Long userId;
	private String rcPath;
	private String insurancePath;

}
