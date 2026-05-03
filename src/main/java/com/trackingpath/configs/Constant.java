package com.trackingpath.configs;

import java.util.LinkedList;
import java.util.List;

import com.trackingpath.dtos.UserModulePermission;


public class Constant {

	public static List<UserModulePermission> getDefaultPermission() {
		LinkedList<UserModulePermission> modulesList = new LinkedList<UserModulePermission>();
		modulesList.add(new UserModulePermission("Dashboard",true,false,false));
		modulesList.add(new UserModulePermission("Object",true,false,false));
		modulesList.add(new UserModulePermission("Expense",true,false,false));
		modulesList.add(new UserModulePermission("Geofence",true,false,false));
		modulesList.add(new UserModulePermission("User",true,false,false));
		modulesList.add(new UserModulePermission("Driver",true,false,false));
		modulesList.add(new UserModulePermission("Maintenance",true,false,false));
		modulesList.add(new UserModulePermission("Alert",true,false,false));
		modulesList.add(new UserModulePermission("Sms",true,false,false));
		modulesList.add(new UserModulePermission("Email",true,false,false));
		modulesList.add(new UserModulePermission("Gprs",true,false,false));
		modulesList.add(new UserModulePermission("Sensor",true,false,false));
		modulesList.add(new UserModulePermission("Task",true,false,false));
		modulesList.add(new UserModulePermission("Subscription",true,false,false));
		modulesList.add(new UserModulePermission("Setup",true,false,false));
        modulesList.add(new UserModulePermission("Commands",true,false,false));
        modulesList.add(new UserModulePermission("Poi",true,false,false));
        modulesList.add(new UserModulePermission("Route",true,false,false));
        modulesList.add(new UserModulePermission("Report",true,false,false));
        modulesList.add(new UserModulePermission("Chat",true,false,false));
		return modulesList;

	}

}
   