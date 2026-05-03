package com.trackingpath.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class CalibrationDetailDTO {
	@JsonIgnore
	private long id;
	
	@JsonIgnore
	private long device_id;
	@JsonIgnore
	private long sensor_type_id;
	
	  @JsonProperty("x")
	private long calibrated_param;
	  @JsonProperty("y")
	private long calibrated_value;
	  @JsonIgnore
	private long device_sensor_mapping_id;
	
	
	
}
