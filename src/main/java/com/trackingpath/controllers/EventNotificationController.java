package com.trackingpath.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.NotificationDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.EventNotificationService;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
public class EventNotificationController {

	private final EventNotificationService eventNotificationService;
    private final AuthenticationService authenticationService;
    
    @GetMapping("/getAllEvents")
    public List<NotificationDTO> getNotifications(
            @RequestParam(name = "stime", required = false) String stime,
            @RequestParam(name = "etime", required = false) String etime,
            @RequestParam(name = "alert_type", required = false, defaultValue = "") String alertType,
            @RequestParam(name = "deviceIds", required = false) String deviceIds
    ) {
        Users user = authenticationService.getCurrentUser();
        return eventNotificationService.getNotifications(user, stime, etime, alertType, deviceIds);
    }
    @GetMapping("/recentEvents")
    public ResponseEntity<List<NotificationDTO>> getRecentAlertNotificationData(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        // Get user from session or JWT
    	@SuppressWarnings("static-access")
		Users user = authenticationService.getCurrentUser();    
        List<NotificationDTO> notifications = eventNotificationService.getAlertNotificationPopUp(
                user, start, end );

        return ResponseEntity.ok(notifications);
    }

}
