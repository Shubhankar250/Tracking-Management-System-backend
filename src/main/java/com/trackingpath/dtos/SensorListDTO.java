package com.trackingpath.dtos;

import lombok.Data;

@Data
public class SensorListDTO {
	
	private long id;
	private String name;
	private String sensor_type_name;
	private String parameter;
	
	
	

    public SensorListDTO(Long id, String name, String sensor_type_name , String parameter) {
        this.id = id;
        this.name = name;
        this.sensor_type_name = sensor_type_name ;
        this.parameter = parameter;
    }

}
