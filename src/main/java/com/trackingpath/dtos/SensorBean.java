package com.trackingpath.dtos;

import java.util.List;

public class SensorBean {
	private List<CalibrationDetailDTO> calibratedDetailBean;
	private DeviceSensorMappingDTO deviceSensorMappingBean;
	

	public List<CalibrationDetailDTO> getCalibratedDetailBean() {
		return calibratedDetailBean;
	}

	public void setCalibratedDetailBean(List<CalibrationDetailDTO> calibratedDetailBean) {
		this.calibratedDetailBean = calibratedDetailBean;
	}

	public DeviceSensorMappingDTO getDeviceSensorMappingBean() {
		return deviceSensorMappingBean;
	}

	public void setDeviceSensorMappingBean(DeviceSensorMappingDTO deviceSensorMappingBean) {
		this.deviceSensorMappingBean = deviceSensorMappingBean;
	}
	
	

}
