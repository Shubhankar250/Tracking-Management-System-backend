package com.trackingpath.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddGeoGroupDTO {
	private List<String>  group_names;
	private List<Long> deletedIds;
	
	
	
	

}
