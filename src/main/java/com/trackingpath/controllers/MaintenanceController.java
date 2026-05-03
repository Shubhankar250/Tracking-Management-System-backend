package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.MaintenanceDto;
import com.trackingpath.dtos.MaintenanceResponseDto;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.MaintenanceService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService service;

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    private ActivityLogService activityLogService;

    // CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addMaintenance(@RequestBody MaintenanceDto dto, HttpServletRequest request) {
        Users user = authenticationService.getCurrentUser();

        boolean result = service.addMaintenanceData(dto,user);

        if (result) {
        	activityLogService.createActivity("NEW SERVICE CREATED",
						"SERVICE(" + dto.getServiceName() + ") CREATED BY " + user.getUsername(), user, request);
            return ResponseEntity.ok("Added Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed!");
        }
    }

    // READ ALL
    @GetMapping
    public Map<String, Object> getMaintenanceData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String search) {


        Users user = authenticationService.getCurrentUser();

        if (user != null) {
            return service.getMaintenanceData(user, page, pageSize,search);
        } else {
            return null;
        }
    }

    // CHANGE STATUS (PARTIAL UPDATE)
    @PutMapping("/{id}/status")
    public String changeMaintenanceStatus(
            @PathVariable long id,
            @RequestParam String status, HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();
        boolean response = service.changeMaintenanceStatus(id, status, user);
        activityLogService.createActivity("SERVICE STATUS UPDATED",
				"SERVICE STATUS WITH ID(" + id + ") UPDATED BY " + user.getUsername(), user, request);

        return response ? "status updated successfully" : "failed";
    }

    // READ BY ID
    @GetMapping("/{id}")
    public MaintenanceResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // UPDATE
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateMaintenanceData(@RequestBody MaintenanceDto bean , HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();
        boolean status = service.updateMaintenanceData(bean, user);

        if (status) {
        	activityLogService.createActivity("SERVICE UPDATED",
					"SERVICE(" + bean.getServiceName() + ") UPDATED BY " + user.getUsername(), user, request);	
            return "Updated Suuccessfully";
        } else {
            return "Failed!";
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteMaintenanceService(@PathVariable long id, HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();
        boolean status = service.deleteMaintenanceService(id, user);

        if (status) {
        	activityLogService.createActivity("SERVICE DELETED",
					"SERVICE DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);	
            return "Deleted Suuccessfully";
        } else {
            return "Failed!";
        }
    }
}
