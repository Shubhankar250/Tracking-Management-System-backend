package com.trackingpath.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor
@Builder
public class GeoGroupDTO {
	
	private Long  group_id;
	private String group_name;

	
	 GeoGroupDTO(Long id,String name) {
		this.group_id= id;
		this.group_name = name;
	}
}
