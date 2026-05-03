package com.trackingpath.dtos;

import lombok.Data;

@Data
public class DeviceForUserBean {
	   private Long  id;
	    private String name;
	
		public DeviceForUserBean(Long  id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
	    
	    
	    
	    
}
