package com.trackingpath.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceReportDto {
	 private String name;
	 private String startPoint;
	 private String endPoint;
	 private String distance;
	    
	 private String stime;
	 private String etime;

}
