package com.trackingpath.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.AlertDataDTO;
import com.trackingpath.dtos.AlertSettingDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.IncorrectArgumentException;
import com.trackingpath.exceptions.ResourceNotFoundException;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AlertService;
import com.trackingpath.services.AuthenticationService;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequestMapping("/alerts")
@RestController
@RequiredArgsConstructor
public class AlertController {
	
    private final AlertService alertService;
    private final AuthenticationService authenticationService;
    @Autowired
    private ActivityLogService activityLogService;
    // CREATE alert
    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertSettingDTO dto,HttpServletRequest request) {
        @SuppressWarnings("static-access")
		Users user = authenticationService.getCurrentUser();
        Long id = alertService.createAlert(dto, user);

        if (id != null && id > 0) {
        	activityLogService.createActivity("NEW ALERT CREATED",
					"ALERT(" + dto.getAlertName() + ") CREATED BY " + user.getUsername(), user, request);
            return ResponseEntity.ok("Alert created successfully!");
        }
        return ResponseEntity.status(500).body("Failed to create alert");
    }

    // GET all alerts
    @GetMapping
    public ResponseEntity<Page<AlertDataDTO>> getAllAlerts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
    	@SuppressWarnings("static-access")
        Users user = authenticationService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<AlertDataDTO> alerts = alertService.getAlertsData(user, search, pageable);
        return ResponseEntity.ok(alerts);
    }

    // GET alert by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertById(@PathVariable("id") long alert_id) {
        @SuppressWarnings("static-access")
        Users user = authenticationService.getCurrentUser();
        try {
            AlertSettingDTO dto = alertService.getAlertById(alert_id, user);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException ex) {
            // Return only the message in the body, with status 200
            return ResponseEntity.ok(
                Map.of("message", ex.getMessage())
            );
        }
    }


    // DELETE alert
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAlert(@PathVariable("id") long alert_id,HttpServletRequest request) {

        @SuppressWarnings("static-access")
        Users user = authenticationService.getCurrentUser();

        try {
            boolean deleted = alertService.deleteAlert(alert_id, user);

            if (deleted) {
            	activityLogService.createActivity("ALERT DELETED",
						"ALERT DELETED WITH ID (" + alert_id + ") DELETED BY " + user.getUsername(), user, request);
                return ResponseEntity.ok(Map.of("message", "Alert deleted successfully!"));
            } else {
                // Alert not found or user not authorized
                return ResponseEntity.ok(Map.of("message", "Alert not found"));
            }

        } catch (Exception ex) {
            // In case of unexpected errors
            return ResponseEntity.ok(Map.of("message", ex.getMessage()));
        }
    }

    
	/*
	 * @PutMapping("/updateAlert") public ResponseEntity<Map<String, String>>
	 * updateAlert(@RequestBody AlertSettingDTO dto,HttpServletRequest request) {
	 * 
	 * if (dto.getId() == null || dto.getId() <= 0) { return
	 * ResponseEntity.badRequest() .body(Map.of("message",
	 * "Valid alert id is required")); }
	 * 
	 * @SuppressWarnings("static-access") Users user =
	 * authenticationService.getCurrentUser();
	 * 
	 * try { boolean updated = alertService.updateAlert(dto, user);
	 * activityLogService.createActivity("ALERT UPDATED", "ALERT("
	 * +dto.getAlertName() + ") UPDATED BY " + user.getUsername(), user, request);
	 * return updated ? ResponseEntity.ok(Map.of("message",
	 * "Alert updated successfully!")) : ResponseEntity.status(HttpStatus.NOT_FOUND)
	 * .body(Map.of("message", "Alert not found"));
	 * 
	 * } catch (IncorrectArgumentException ex) { return ResponseEntity.badRequest()
	 * .body(Map.of("message", ex.getMessage())); } }
	 */
    @PutMapping("/updateAlert")
    public ResponseEntity<String> updateAlert(
            @RequestBody AlertSettingDTO dto,
            HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
        }

        boolean status = alertService.updateAlertData(dto, user);

        if (!status) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed!");
        }

        Long alertId = dto.getId();

        // Device Mapping
        if (dto.getAlertDeviceMappingDTO() != null &&
            dto.getAlertDeviceMappingDTO().getDeviceIds() != null &&
            !dto.getAlertDeviceMappingDTO().getDeviceIds().isEmpty()) {

            alertService.updateAlertDeviceMapping(dto, alertId, user);
            alertService.updateAlertDeviceCommand(dto, alertId, user);
        }

        // Alert Details
        alertService.updateAlertDetails(dto, alertId, user);

        // Geofence
        if (dto.getAlertGeofenceMappingDTO() != null &&
            dto.getAlertGeofenceMappingDTO().getGeofenceIds() != null &&
            !dto.getAlertGeofenceMappingDTO().getGeofenceIds().isEmpty()) {

            alertService.updateAlertGeofence(dto, alertId, user);
        }

        // Route
        if (dto.getAlertRouteMappingDTO() != null &&
            dto.getAlertRouteMappingDTO().getRouteIds() != null &&
            !dto.getAlertRouteMappingDTO().getRouteIds().isEmpty()) {

            alertService.updateAlertRoute(dto, alertId, user);
        }

        // Schedule
        alertService.updateAlertScheduling(dto, alertId, user);

        // Notification
        alertService.updateAlertNotification(dto, alertId, user);

        // Alert Users
        alertService.updateAlertUsers(dto, alertId, user);

        activityLogService.createActivity(
                "ALERT UPDATED",
                "ALERT(" + dto.getAlertName() + ") UPDATED BY " + user.getUsername(),
                user,
                request
        );

        return ResponseEntity.ok("Alert Updated successfully!");
    }
    
    @PutMapping("/{id}/changeAlertStatus")
    public ResponseEntity<Map<String, String>> toggleAlertStatus(
            @PathVariable("id") Long alertId,
            HttpServletRequest request) {

        @SuppressWarnings("static-access")
        Users user = authenticationService.getCurrentUser();

        try {
            boolean updated = alertService.toggleAlertStatus(alertId, user);

            if (updated) {

                activityLogService.createActivity(
                        "ALERT STATUS CHANGED",
                        "ALERT STATUS TOGGLED FOR ID (" + alertId + ") BY " + user.getUsername(),
                        user,
                        request
                );

                return ResponseEntity.ok(
                        Map.of("message", "Alert status changed successfully!")
                );
            } else {
                return ResponseEntity.ok(
                        Map.of("message", "Alert not found or unauthorized")
                );
            }

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.ok(
                    Map.of("message", ex.getMessage())
            );
        } catch (IncorrectArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }
    
    
    
    @GetMapping("/command-names")
    public List<String> getCommandName(
            @RequestParam(name = "alertType", defaultValue = "") String alertType) {
        return alertService.getCommandName(alertType);
    }
    
    
}
