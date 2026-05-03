package com.trackingpath.services;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.DeliveryDto;
import com.trackingpath.dtos.PickupDTO;
import com.trackingpath.dtos.TaskDTO;
import com.trackingpath.entities.Task;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.TaskMapper;
import com.trackingpath.repositories.TaskRepository;

@Service

public class TaskService {
	@Autowired

	TaskRepository repo;
	@Autowired
	TaskMapper taskMapper;

	public boolean addTaskData(TaskDTO bean, Users user) {

		if (bean.getStatus() == null || bean.getStatus().trim().isEmpty()) {
			bean.setStatus("Pending");
		}

		Task t = taskMapper.mapToEntity(bean, user.getId());
		repo.save(t);
		return true;
	}

	public Page<TaskDTO> getTaskData(Users user,String start_time,String end_time,long deviceId,String search,Pageable pageable) {

	    Timestamp st = null;
	    Timestamp et = null;

	    if (start_time != null && !start_time.trim().isEmpty()) {
	        st = Timestamp.valueOf(start_time.trim() + " 00:00:00");
	    }

	    if (end_time != null && !end_time.trim().isEmpty()) {
	        et = Timestamp.valueOf(end_time.trim() + " 23:59:59");
	    }

	    String searchParam = (search == null) ? "" : search.trim();

	    Page<Task> pageResult = repo.getTaskData(
	            st,
	            et,
	            deviceId,
	            searchParam,
	            pageable
	    );

	    return pageResult.map(taskMapper::mapToDTO);
	}
	public TaskDTO getTaskUpdate(long id) {
		return repo.findByIdWithDevice(id).map(taskMapper::mapToDTO).orElse(null);
	}

	public boolean deleteTaskService(long id, Users user) {
		repo.deleteById(id);
		return true;
	}

	public boolean updateTaskData(TaskDTO bean) {

	

		return repo.findById(bean.getId()).map(t -> {
			taskMapper.updateEntity(t, bean);
			repo.save(t);
			return true;
		}).orElse(false);
	}

	public boolean updatePickupdata(PickupDTO bean) {

	    return repo.findById(bean.getId()).map(t -> {

	        t.setPickedUpTime(parse(bean.getPicked_up_time()));
	        t.setPickupLatitude(bean.getPickup_latitude());
	        t.setPickupLongitude(bean.getPickup_longitude());
	        t.setStatus(bean.getStatus());

	        // save image if present
	        if (bean.getPickupImage() != null && !bean.getPickupImage().isEmpty()) {
	            t.setPickupImage(bean.getPickupImage());
	        }

	        repo.save(t);
	        return true;

	    }).orElse(false);
	}

	  public boolean updatedelivereddata(DeliveryDto bean) {

		    return repo.findById(bean.getId()).map(t -> {

		        t.setStatus(bean.getStatus());
		        t.setDeliveryLatitude(bean.getDelivery_latitude());
		        t.setDeliveryLongitude(bean.getDelivery_longitude());
		        t.setDeliveredTime(parse(bean.getDelivered_time()));

		        if (bean.getDeliveryImage() != null && !bean.getDeliveryImage().isEmpty()) {
		            t.setDeliveryImage(bean.getDeliveryImage());
		        }

		        repo.save(t);
		        return true;

		    }).orElse(false);
		}
	
	
	    public Timestamp parse(String s) {
	        if (s == null || s.isEmpty()) return null;
	        s = s.replace("T", " ");
	        if (s.length() == 16) s += ":00";
	        return Timestamp.valueOf(s);
	    }
	
}
