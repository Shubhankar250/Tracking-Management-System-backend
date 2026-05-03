package com.trackingpath.dtos;

import java.util.LinkedHashMap;

import lombok.Data;

@Data
public class DashboardSettingDTO {
	    private double totalDistanceOfDay;
	    private int totalAlerts;
	    private double averageDistancePerVehicle;

	    private LinkedHashMap<String, Long> vehicleGpsMap;
	    private LinkedHashMap<String, Long> vehicleMovement;
	    private LinkedHashMap<Integer, Double> hourlyDistanceData;
	    private LinkedHashMap<String, Integer> alertTypes;
	    private LinkedHashMap<String, Long> maintenanceDataMap;

	    private double totalExpense;
}
