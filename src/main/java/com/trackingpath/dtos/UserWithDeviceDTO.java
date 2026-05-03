package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithDeviceDTO {
	  private String type; // USER / DRIVER / TASK

	    private Long id;
	    private String name;
	    private String username;
	    private String roleName;
	    private String deviceName;
}
