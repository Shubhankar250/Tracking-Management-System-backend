package com.trackingpath.dtos;

public class ResultantSensorBean {
	
	private String sensor_name;
	private String icon_name;
	private Object resultant_value;
	private String unit_of_measurement;
	private String type;
	public String getSensor_name() {
		return sensor_name;
	}
	public void setSensor_name(String sensor_name) {
		this.sensor_name = sensor_name;
	}
	public String getIcon_name() {
		return icon_name;
	}
	public void setIcon_name(String icon_name) {
		this.icon_name = icon_name;
	}
	
	public String getUnit_of_measurement() {
		return unit_of_measurement;
	}
	public void setUnit_of_measurement(String unit_of_measurement) {
		this.unit_of_measurement = unit_of_measurement;
	}
	public Object getResultant_value() {
		return resultant_value;
	}
	public void setResultant_value(Object resultant_value) {
		this.resultant_value = resultant_value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	

}
