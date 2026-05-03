package com.trackingpath.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "devices_geo_mapping")

public class Dgm {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "user_id")
	    private Long userId;

	    @Column(name = "deviceid")
	    private Long deviceId;

	    @Column(name = "cityid")
	    private Long cityId;

	    @Column(name = "zoneid")
	    private Long zoneId;

	    @Column(name = "wardid")
	    private Long wardId;

	    @Column(name = "shiftid")
	    private Long shiftId;

	    @Column(name = "routeid")
	    private Long routeId;

	    @Column(name = "creationtime")
	    private LocalDateTime creationTime;

	    @Column(name = "vehicle_status")
	    private String vehicleStatus;

	    @Column(name = "reason")
	    private String reason;

	    @Column(name = "default_route_id")
	    private Long defaultRouteId;

	    @Column(name = "default_device_id")
	    private Long defaultDeviceId;


	
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}
		public Long getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(Long deviceId) {
			this.deviceId = deviceId;
		}
		public Long getCityId() {
			return cityId;
		}
		public void setCityId(Long cityId) {
			this.cityId = cityId;
		}
		public Long getZoneId() {
			return zoneId;
		}
		public void setZoneId(Long zoneId) {
			this.zoneId = zoneId;
		}
		public Long getWardId() {
			return wardId;
		}
		public void setWardId(Long wardId) {
			this.wardId = wardId;
		}
		public Long getShiftId() {
			return shiftId;
		}
		public void setShiftId(Long shiftId) {
			this.shiftId = shiftId;
		}
		public Long getRouteId() {
			return routeId;
		}
		public void setRouteId(Long routeId) {
			this.routeId = routeId;
		}
		public LocalDateTime getCreationTime() {
			return creationTime;
		}
		public void setCreationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
		}
		public String getVehicleStatus() {
			return vehicleStatus;
		}
		public void setVehicleStatus(String vehicleStatus) {
			this.vehicleStatus = vehicleStatus;
		}
		public String getReason() {
			return reason;
		}
		public void setReason(String reason) {
			this.reason = reason;
		}
		public Long getDefaultRouteId() {
			return defaultRouteId;
		}
		public void setDefaultRouteId(Long defaultRouteId) {
			this.defaultRouteId = defaultRouteId;
		}
		public Long getDefaultDeviceId() {
			return defaultDeviceId;
		}
		public void setDefaultDeviceId(Long defaultDeviceId) {
			this.defaultDeviceId = defaultDeviceId;
		}


	    
	    
	
}
