package com.trackingpath.controllers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.DashboardSettingDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.DashboardService;

@RequestMapping("/api/dashboard")
@RestController
public class DashboardController {
	    private final DashboardService dashboardService;
	    private final AuthenticationService authenticationService;

	    public DashboardController(DashboardService dashboardService,
	                               AuthenticationService authenticationService) {
	        this.dashboardService = dashboardService;
	        this.authenticationService = authenticationService;
	    }

	    @GetMapping("/data")
	    public DashboardSettingDTO getDashboardData() {
	        Users user = authenticationService.getCurrentUser();
	        return dashboardService.getDashboardData(user);
	    }
	

}