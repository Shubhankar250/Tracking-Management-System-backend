package com.trackingpath.services;

import java.util.List;

import com.trackingpath.dtos.BreakdownDetailsDto;
import com.trackingpath.dtos.DriverDashboardResponse;
import com.trackingpath.dtos.DriverLiveTripDto;
import com.trackingpath.dtos.PassengerListDto;
import com.trackingpath.dtos.StopListDto;
import com.trackingpath.dtos.VehicleEventRequest;
import com.trackingpath.entities.StaffDetails;
import com.trackingpath.entities.TripVehicleEvent;


public interface DriverDashboardService {
	DriverDashboardResponse getDashboard(Long driverId); 
	List<StopListDto> getStopList(Long driverId);
	DriverLiveTripDto getLiveTrip(Long driverId);
	List<PassengerListDto> getPassengerList(Long driverId, String pickupStop, String attendanceStatus);
	List<StaffDetails> getAllStaffs();
	void markAllBoarded(Long driverId, String pickupStop, String attendanceStatus);
	void markAllDeboarded(Long driverId, String pickupStop, String attendanceStatus);
	void markAllAbsent(Long driverId, String pickupStop, String attendanceStatus);
	void markPassengerBoarded(Long passengerExecutionId);
	void markPassengerAbsent(Long passengerExecutionId);
	void reportBreakdown(VehicleEventRequest request);
	void replaceVehicle(VehicleEventRequest request);
	BreakdownDetailsDto getBreakdownDetails(Long driverId);
	List<TripVehicleEvent> getVehicleEventsByDriver(Long driverId);
}
