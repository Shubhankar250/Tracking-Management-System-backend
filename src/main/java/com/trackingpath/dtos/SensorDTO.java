package com.trackingpath.dtos;

import java.util.List;

import lombok.Data;
@Data
public class SensorDTO {
	private List<CalibrationDetailDTO> calibratedDetailBean;
	private DeviceSensorMappingDTO deviceSensorMappingBean;


}
