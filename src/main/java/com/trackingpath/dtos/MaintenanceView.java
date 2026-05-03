package com.trackingpath.dtos;

import java.time.LocalDate;

public interface MaintenanceView {

	LocalDate getLastServiceDate();
	Long getLastServiceKm();
	

	String getServiceName();

}
