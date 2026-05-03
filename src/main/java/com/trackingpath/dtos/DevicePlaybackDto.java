package com.trackingpath.dtos;

import lombok.Data;

@Data
public class DevicePlaybackDto {


	    private long id;
	    private String created_at;
	    private long user_id;
	    private long device_id;
	    private String name;
	    private String uniqueid;
	    private String model;
	    private String vin;
	    private String plate_number;
	    private long groupid;
	    private String trailer;
	    private String sim_card_number;
	    private String sim_imsi_number;
	    private String odometer;
	    private String engine_hours;
	    private String icon_type;
	    private String no_connection_arrow_color;
	    private String stopped_arrow_color;
	    private String moving_arrow_color;
	    private String engine_idle_arrow_color;
	    private String icon_name;
	    private String sources;
	    private String measurement;
	    private double cost_per_liter;
	    private long positionid;
	    private long bettary_percentage;
	    private String lastupdate;
	    private String expire_time;
	    private String device_type;
	    private String category;
	    private String certificate_valid;
	    private String fitness_valid;
	    private String vehicle_status;
	    private String device_modal;
	    private String last_seen;
	    private String last_location;
	    private long parking_id;
	    private long driver_id;
	    private long helper_id;
	    private String driver_name;
	    private String driver_details;
	    private String helper_name;
	    private String parking_name;
	    private double latitude;
	    private double longitude;
	    private long route_id;
	    private String last_command;
	    private long satelite;

	    private String status;

	    private DeviceGroupDto DeviceGroupBean;
	}