package com.trackingpath.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "maintenance_service")
@Data
public class MaintenanceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "service_name")
	private String serviceName;

	/* ------------------- Relations ------------------- */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id")
	private DeviceEntity device;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	private Users admin;

	/* ------------------- Flags ------------------- */

	private Boolean datalist;
	private Boolean popup;

	@Column(name = "odometer_interval_km")
	private Boolean odometerIntervalKm;

	@Column(name = "engine_hour_interval")
	private Boolean engineHourInterval;

	@Column(name = "days_interval")
	private Boolean daysInterval;

	@Column(name = "odometer_left_km")
	private Boolean odometerLeftKm;

	@Column(name = "engine_hours_left")
	private Boolean engineHoursLeft;

	@Column(name = "update_last_service")
	private Boolean updateLastService;

	@Column(name = "days_left")
	private Boolean daysLeft;

	@Column(name = "event_trigger")
	private Boolean eventTrigger;

	/* ------------------- Values ------------------- */

	@Column(name = "odometer_interval_km_val")
	private Long odometerIntervalKmVal;

	@Column(name = "last_service_km")
	private Long lastServiceKm;

	@Column(name = "engine_hour_interval_val")
	private Long engineHourIntervalVal;

	@Column(name = "last_service_hours")
	private Long lastServiceHours;

	@Column(name = "days_interval_val")
	private Long daysIntervalVal;

	@Column(name = "odometer_left_km_val")
	private Long odometerLeftKmVal;

	@Column(name = "engine_hours_left_val")
	private Long engineHoursLeftVal;

	@Column(name = "days_left_val")
	private Long daysLeftVal;

	@Column(name = "last_service_date")
	private LocalDate lastServiceDate;
}
