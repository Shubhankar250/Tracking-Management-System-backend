package com.trackingpath.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DevicesUpdateDto {

	private Long deviceId;
	private String deviceTimezone;
	private String deviceName;
	private String deviceModel;
	private String objectIcon;
	private String deviceImei;
	private Double odometer;
	private String vehicleStatus;

	private Long groupId;
	private String groupName;

	private String username;
	private Long userId;
	private String accountName;

	private String simCardNumber;
	 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime simActivationDate;
	 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime simExpirationDate;

	private String vin;
	 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

	private String tailColor;
	private Integer tailLength;

	private String imgIconName;
	private String imgIconType;

	private String maxSpeed;
	private String minMovingSpeed;
	private String minFuelFillings;
	private String minFuelTheft;
	private String fuelChangeAfterStop;

	private String rcPath;
	private String insurancePath;

	public DevicesUpdateDto(Long deviceId, String deviceTimezone, String deviceName, String deviceModel,
			String objectIcon, String deviceImei, Double odometer, String vehicleStatus,

			Long groupId, String groupName,

			String username, Long userId, String accountName,

			String simCardNumber, LocalDateTime simActivationDate, LocalDateTime simExpirationDate,

			String vin, LocalDateTime installationDate, String plateNumber, String registrationNumber, String owner,

			String fuelMeasureName, BigDecimal fuelMeasurement, BigDecimal fuelCost,

			String iconType, String movingIconColor, String stoppedIconColor, String offlineIconColor,
			String engineIdleColor,

			String tailColor, Integer tailLength, String imgIconName, String imgIconType,

			String maxSpeed, String minMovingSpeed, String minFuelFillings, String minFuelTheft,
			String fuelChangeAfterStop, String rcPath, String insurancePath) {
		this.deviceId = deviceId;
		this.deviceTimezone = deviceTimezone;
		this.deviceName = deviceName;
		this.deviceModel = deviceModel;
		this.objectIcon = objectIcon;
		this.deviceImei = deviceImei;
		this.odometer = odometer;
		this.vehicleStatus = vehicleStatus;
		this.groupId = groupId;
		this.groupName = groupName;
		this.username = username;
		this.userId = userId;
		this.accountName = accountName;
		this.simCardNumber = simCardNumber;
		this.simActivationDate = simActivationDate;
		this.simExpirationDate = simExpirationDate;
		this.vin = vin;
		this.installationDate = installationDate;
		this.plateNumber = plateNumber;
		this.registrationNumber = registrationNumber;
		this.owner = owner;
		this.fuelMeasureName = fuelMeasureName;
		this.fuelMeasurement = fuelMeasurement;
		this.fuelCost = fuelCost;
		this.iconType = iconType;
		this.movingIconColor = movingIconColor;
		this.stoppedIconColor = stoppedIconColor;
		this.offlineIconColor = offlineIconColor;
		this.engineIdleColor = engineIdleColor;
		this.tailColor = tailColor;
		this.tailLength = tailLength;
		this.imgIconName = imgIconName;
		this.imgIconType = imgIconType;
		this.maxSpeed = maxSpeed;
		this.minMovingSpeed = minMovingSpeed;
		this.minFuelFillings = minFuelFillings;
		this.minFuelTheft = minFuelTheft;
		this.fuelChangeAfterStop = fuelChangeAfterStop;
		this.rcPath = rcPath;
		this.insurancePath = insurancePath;
	}
}
