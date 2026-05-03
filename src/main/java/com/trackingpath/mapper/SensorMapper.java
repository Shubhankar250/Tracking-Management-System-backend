package com.trackingpath.mapper;

import com.trackingpath.dtos.DeviceSensorMappingDTO;
import com.trackingpath.dtos.SensorDTO;
import com.trackingpath.entities.DeviceSensorMapping;

import com.trackingpath.entities.Users;

public class SensorMapper {

	public static DeviceSensorMapping toEntity(SensorDTO bean, Users user) {

	    DeviceSensorMappingDTO d = bean.getDeviceSensorMappingBean();

	    DeviceSensorMapping entity = new DeviceSensorMapping();

	    entity.setName(d.getName());
	    entity.setSensorTypeId(d.getSensor_type_id());
	    entity.setParameter(d.getParameter());
	    entity.setType(d.getType());
	    entity.setUnitOfMeasurement(d.getUnit_of_measurement());
	    entity.setIfSensor1(d.getIf_sensor_1());
	    entity.setIfSensor0(d.getIf_sensor_0());
	    entity.setFormula(d.getFormula());
	    entity.setLowestValue(d.getLowest_value());
	    entity.setHighestValue(d.getHighest_value());
	    entity.setIgnoreIgnitionOff(d.isIgnore_ignition_off());
	    entity.setDeviceId(d.getDevice_id());

	    // From logged-in user
	    entity.setUserId(user.getId());
	    entity.setAdminId(user.getAdminId());

	    // JSONB list
	    entity.setCalibrationData(bean.getCalibratedDetailBean());

	    return entity;
	}

    
	public static void updateEntity(DeviceSensorMapping entity, SensorDTO bean) {

	    DeviceSensorMappingDTO d = bean.getDeviceSensorMappingBean();

	    entity.setName(d.getName());
	    entity.setSensorTypeId(d.getSensor_type_id());
	    entity.setParameter(d.getParameter());
	    entity.setType(d.getType());
	    entity.setUnitOfMeasurement(d.getUnit_of_measurement());
	    entity.setIfSensor1(d.getIf_sensor_1());
	    entity.setIfSensor0(d.getIf_sensor_0());
	    entity.setFormula(d.getFormula());
	    entity.setLowestValue(d.getLowest_value());
	    entity.setHighestValue(d.getHighest_value());
	    entity.setIgnoreIgnitionOff(d.isIgnore_ignition_off());
	    entity.setDeviceId(d.getDevice_id());

	    // Update JSONB list
	    entity.setCalibrationData(bean.getCalibratedDetailBean());
	}

	public static DeviceSensorMappingDTO toDto(DeviceSensorMapping entity) {

	    DeviceSensorMappingDTO dto = new DeviceSensorMappingDTO();

	    dto.setId(entity.getId());
	    dto.setName(entity.getName());
	    dto.setSensor_type_id(entity.getSensorTypeId());
	    dto.setParameter(entity.getParameter());
	    dto.setType(entity.getType());
	    dto.setUnit_of_measurement(entity.getUnitOfMeasurement());
	    dto.setIf_sensor_1(entity.getIfSensor1());
	    dto.setIf_sensor_0(entity.getIfSensor0());
	    dto.setFormula(entity.getFormula());
	    dto.setLowest_value(entity.getLowestValue());
	    dto.setHighest_value(entity.getHighestValue());
	    dto.setIgnore_ignition_off(entity.getIgnoreIgnitionOff());
	    dto.setDevice_id(entity.getDeviceId());

	    // JSONB LIST → DTO LIST
	    dto.setCalibrationData(entity.getCalibrationData());

	    return dto;
	}


}
