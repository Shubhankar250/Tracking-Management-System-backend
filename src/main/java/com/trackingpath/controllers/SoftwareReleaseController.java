package com.trackingpath.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.SoftwareReleaseDTO;
import com.trackingpath.entities.SoftwareReleaseEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.SoftwareReleaseService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/software")
public class SoftwareReleaseController {

	
	
	 @Autowired
	    private SoftwareReleaseService service;

	    @Autowired
	    private AuthenticationService authenticationService;
	    @Autowired
	    private ActivityLogService activityLogService;

	    // CREATE
	    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	    public String addSoftwareRelease(@RequestBody SoftwareReleaseDTO dto,HttpServletRequest request) {

	        Users user = authenticationService.getUser();
	        activityLogService.createActivity("NEW SoftwareRelease CREATED",
					"SoftwareRelease(" + dto.getText() + ") CREATED BY " + user.getUsername(), user, request);

	        if (service.addSoftwareRelease(dto, user))
	            return "Added Successfully";
	        else
	            return "Failed!";
	    }

	    // READ ALL
	    @GetMapping
	    public Map<String, Object> getSoftwareReleases(   @RequestParam(defaultValue = "1") int page,
	            @RequestParam(defaultValue = "10") int pageSize,
	            @RequestParam(defaultValue = "") String search) {

	        Users user = authenticationService.getUser();
	        return service.getSoftwareReleaseList(user, page, pageSize, search);
	    }

	    // READ BY ID
	    @GetMapping("/{id}")
	    public SoftwareReleaseDTO getSoftwareRelease(@PathVariable long id) {
	        return service.getSoftwareReleaseById(id);
	    }

	    // UPDATE
	    @PutMapping
	    public String updateSoftwareRelease(@RequestBody SoftwareReleaseEntity SoftwareRelease, HttpServletRequest request) {

	        Users user = authenticationService.getUser();

	        if (service.updateSoftwareRelease(SoftwareRelease, user)) {

	            // 🔥 ACTIVITY LOG
	            activityLogService.createActivity(
	                    "SoftwareRelease_UPDATED",
	                    "SoftwareRelease (" + SoftwareRelease.getText() + ") UPDATED BY " + user.getUsername(),
	                    user,
	                    request
	            );

	            return "Updated Successfully";
	        } else {
	            return "Failed!";
	        }
	    }


	    // DELETE
	    @DeleteMapping("/{id}")
	    public String deleteSoftwareRelease(@PathVariable long id,HttpServletRequest request) {
	    	Users user = authenticationService.getUser();
	        if (service.deleteSoftwareRelease(id)) {
	        	
	        	activityLogService.createActivity("SoftwareRelease DELETED",
						"SoftwareRelease DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);	
	            return "Deleted Successfully";
	            
	        } else {
	            return "Failed!";
	    }
	    }
	
	
}
