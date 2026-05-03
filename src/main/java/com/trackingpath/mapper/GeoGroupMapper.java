package com.trackingpath.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.trackingpath.dtos.GeoGroupDTO;
import com.trackingpath.entities.GeoGroups;
import com.trackingpath.entities.Users;

@Component
public class GeoGroupMapper {
	
	  public GeoGroups toEntity(GeoGroupDTO dto, Users user) {
	        return GeoGroups.builder()
	                .name(dto.getGroup_name())
	                .user_id(user.getId())
	                .admin_id(user.getAdminId())
	                .creation_time(LocalDateTime.now())
	                .build();
	    }

}
