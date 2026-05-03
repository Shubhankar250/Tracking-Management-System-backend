package com.trackingpath.dtos;

import java.util.List;

public class DGMDTO {
	
	 private String group_name;
	 private List<Long> deviceIds;
	 private long group_id;
	 private String device_name;
	    private List<String> group_names;
	    private List<Integer> deletedIds;
	    
	 
	
	public List<Long> getDeviceIds() {
		return deviceIds;
	}
	public void setDeviceIds(List<Long> deviceIds) {
		this.deviceIds = deviceIds;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	public long getGroup_id() {
		return group_id;
	}
	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}
	public String getDevice_name() {
		return device_name;
	}
	public void setDevice_name(String device_name) {
		this.device_name = device_name;
	}
	public List<String> getGroup_names() {
		return group_names;
	}
	public void setGroup_names(List<String> group_names) {
		this.group_names = group_names;
	}
	public List<Integer> getDeletedIds() {
		return deletedIds;
	}
	public void setDeletedIds(List<Integer> deletedIds) {
		this.deletedIds = deletedIds;
	}
	 
	 

}
