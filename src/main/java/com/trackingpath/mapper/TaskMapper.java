package com.trackingpath.mapper;

import java.sql.Timestamp;

import org.springframework.stereotype.Component;

import com.trackingpath.dtos.TaskDTO;
import com.trackingpath.entities.Task;
@Component
public class TaskMapper {
	
	
	  public Task mapToEntity(TaskDTO b, Long userId) {
	        Task t = new Task();
	        t.setName(b.getName());
	        t.setObjectId(b.getObject());
	        t.setDescription(b.getDescription());
	        t.setPriority(b.getPriority());
	        t.setStatus(b.getStatus());
	        t.setPickupAddress(b.getPickup_address());
	        t.setDeliveryAddress(b.getDelivery_address());
	        t.setPickupStartTime(parse(b.getPickup_start_time()));
	        t.setPickupEndTime(parse(b.getPickup_end_time()));
	        t.setDeliveryStartTime(parse(b.getDelivery_start_time()));
	        t.setDeliveryEndTime(parse(b.getDelivery_end_time()));
	        t.setPickupLatitude(b.getPickup_latitude());
	        t.setPickupLongitude(b.getPickup_longitude());

	        t.setDeliveryLatitude(b.getDelivery_latitude());
	        t.setDeliveryLongitude(b.getDelivery_longitude());
	        t.setUserId(userId);
	        return t;
	    }

	    public TaskDTO mapToDTO(Task t) {
	        TaskDTO b = new TaskDTO();
	        b.setId(t.getId());
	        b.setName(t.getName());
	        b.setObject(t.getObjectId());
	        b.setPriority(t.getPriority());
	        b.setDescription(t.getDescription());
	        b.setStatus(t.getStatus());
	        b.setPickup_address(t.getPickupAddress());
	        b.setDelivery_address(t.getDeliveryAddress());
	        b.setPickup_start_time(String.valueOf(t.getPickupStartTime()));
	        b.setPickup_end_time(String.valueOf(t.getPickupEndTime()));
	        b.setDelivery_start_time(String.valueOf(t.getDeliveryStartTime()));
	        b.setDelivery_end_time(String.valueOf(t.getDeliveryEndTime()));

	        b.setPickup_latitude(t.getPickupLatitude());
	        b.setPickup_longitude(t.getPickupLongitude());

	        b.setDelivery_latitude(t.getDeliveryLatitude());
	        b.setDelivery_longitude(t.getDeliveryLongitude());
	        if (t.getDevice() != null) {
	            b.setDevice_name(t.getDevice().getName());
	        }
	        return b;
	    }

	    public void updateEntity(Task t, TaskDTO b) {
	        if (b.getName() != null) t.setName(b.getName());
	        if (b.getPriority() != null) t.setPriority(b.getPriority());
	        if (b.getDescription() != null) t.setDescription(b.getDescription());
	        if (b.getPickup_address() != null) t.setPickupAddress(b.getPickup_address());
	        if (b.getDelivery_address() != null) t.setDeliveryAddress(b.getDelivery_address());
	        if (b.getObject() != 0) t.setObjectId(b.getObject());
	        if (b.getPickup_start_time() != null) t.setPickupStartTime(parse(b.getPickup_start_time()));
	        if (b.getPickup_end_time() != null) t.setPickupEndTime(parse(b.getPickup_end_time()));
	        if (b.getDelivery_start_time() != null) t.setDeliveryStartTime(parse(b.getDelivery_start_time()));
	        if (b.getDelivery_end_time() != null) t.setDeliveryEndTime(parse(b.getDelivery_end_time()));
	        if (b.getPickup_latitude() != null) t.setPickupLatitude(b.getPickup_latitude());

	        if (b.getPickup_longitude() != null)  t.setPickupLongitude(b.getPickup_longitude());

	        if (b.getDelivery_latitude() != null) t.setDeliveryLatitude(b.getDelivery_latitude());

	        if (b.getDelivery_longitude() != null) t.setDeliveryLongitude(b.getDelivery_longitude());
	    }
	
	    public Timestamp parse(String s) {
	        if (s == null || s.isEmpty()) return null;
	        s = s.replace("T", " ");
	        if (s.length() == 16) s += ":00";
	        return Timestamp.valueOf(s);
	    }

}
