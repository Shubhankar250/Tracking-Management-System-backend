package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
	   private Long id;
	    private String name;
	    private String designation;
	    private String email;
	    private String employeeCode;
	    private String mobileNumber;

}
