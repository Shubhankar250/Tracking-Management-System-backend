package com.trackingpath.dtos;

import java.util.List;

import lombok.Data;

@Data
public class DeviceSensorMappingDTO {
	
	private long id;
	private String name;
	private long sensor_type_id;
	private String parameter;
	private String type;
	private String unit_of_measurement;
	private String if_sensor_1;
	private String if_sensor_0;
	private String formula;
	private double lowest_value;
	private double highest_value;
	private boolean ignore_ignition_off;
	private long device_id;
	private long user_id;
	private long admin_id;
	private String sensor_type_name;
	private String icon_name;
//	private SensorBean sensor;

	 private List<CalibrationDetailDTO> calibrationData;
	

}
