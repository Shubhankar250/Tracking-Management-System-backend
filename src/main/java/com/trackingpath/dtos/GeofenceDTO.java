package com.trackingpath.dtos;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GeofenceDTO {
	
	 private Long id; 
	private String pcts_name;

	private String color;
	
	private Object geom;
	private String pcts_type;
	private String geo_group;
	private String speed_limit;
	private double radius;
	

}
