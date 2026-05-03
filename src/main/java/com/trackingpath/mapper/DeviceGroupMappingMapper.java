package com.trackingpath.mapper;


import com.trackingpath.entities.DeviceGroupMapping;
import com.trackingpath.dtos.DGMDTO;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.GroupEntity;
import com.trackingpath.entities.Users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeviceGroupMappingMapper {

	   public static DeviceGroupMapping toEntity(Long deviceId, GroupEntity group, Users user, Users admin) {
	        if (deviceId == null || group == null || user == null || admin == null) return null;

	        DeviceGroupMapping mapping = new DeviceGroupMapping();
	        mapping.setDevice(new DeviceEntity());
	        mapping.getDevice().setId(deviceId);
	        mapping.setGroup(group);
	        mapping.setUserId(user.getId());
	        mapping.setAdminId(admin.getId());
	        return mapping;
	    }

	   public static List<DeviceGroupMapping> toEntities(DGMDTO bean, Users user) {
	        List<DeviceGroupMapping> mappings = new ArrayList<>();

	        for (Long deviceId : bean.getDeviceIds()) {
	            DeviceGroupMapping dgm = DeviceGroupMapping.builder()
	                    .device(DeviceEntity.builder().id(deviceId).build())
	                    .group(GroupEntity.builder().id(bean.getGroup_id()).build())
	                    .userId(user.getId())
	                    .adminId(user.getAdminId())
	                    .creationTime(LocalDateTime.now())
	                    .build();
	            mappings.add(dgm);
	        }

	        return mappings;
	    }
	}

