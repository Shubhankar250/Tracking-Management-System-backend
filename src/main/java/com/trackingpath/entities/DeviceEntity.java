package com.trackingpath.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	@Pattern(regexp = "\\d+", message = "uniqueid must contain only numeric values")
	private String uniqueid;


	@Column(name = "sim_card_number")
	private String simCardNumber;

	@Column(name = "sim_activation_date")
	private LocalDateTime simActivationDate;

	@Column(name = "sim_expiration_date")
	private LocalDateTime simExpirationDate;

	private String vin;

	@Column(name = "device_model")
	private String deviceModel;

	@Column(name = "installation_date")
	private LocalDateTime installationDate;

	@Column(name = "plate_number")
	private String plateNumber;

	@Column(name = "registration_number")
	private String registrationNumber;

	private String owner;

	@Column(name = "fuel_measure_name")
	private String fuelMeasureName;

	@Column(name = "fuel_measurement")
	private BigDecimal fuelMeasurement;

	@Column(name = "fuel_cost")
	private BigDecimal fuelCost;

	@Column(name = "icon_type")
	private String iconType;

	@Column(name = "moving_icon_color")
	private String movingIconColor;

	@Column(name = "stopped_icon_color")
	private String stoppedIconColor;

	@Column(name = "offline_icon_color")
	private String offlineIconColor;

	@Column(name = "engine_idle_color")
	private String engineIdleColor;

	private String status;
	private String sensors;

	@Column(name = "tail_color")
	private String tailColor;

	@Column(name = "tail_length")
	private Integer tailLength;

	@Column(name = "img_icon_name")
	private String imgIconName;

	@Column(name = "img_icon_type")
	private String imgIconType;

	private Double odometer;

	@Column(name = "vehicle_status")
	private String vehicleStatus;

	@Column(name = "max_speed")
	private String maxSpeed;

	@Column(name = "min_moving_speed")
	private String minMovingSpeed;

	@Column(name = "min_fuel_fillings")
	private String minFuelFillings;

	@Column(name = "min_fuel_theft")
	private String minFuelTheft;

	@Column(name = "fuel_change_after_stop")
	private String fuelChangeAfterStop;

	@Column(name = "object_icon")
	private String objectIcon;

	private String devicetimezone;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "rc_path")
	private String rcPath;

	@Column(name = "insurance_path")
	private String insurancePath;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
