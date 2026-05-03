package com.trackingpath.dtos;

public class UserModulePermission {


	private String permission;
	private boolean read;
    private boolean write;
    private boolean delete;
    
    
    
    
	public UserModulePermission() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	public UserModulePermission(String permission, boolean read, boolean write, boolean delete) {
		super();
		this.permission = permission;
		this.read = read;
		this.write = write;
		this.delete = delete;
	}



	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public boolean isWrite() {
		return write;
	}
	public void setWrite(boolean write) {
		this.write = write;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	
	
	

	

}
