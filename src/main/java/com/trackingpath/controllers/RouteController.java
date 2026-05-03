package com.trackingpath.controllers;

import com.trackingpath.dtos.DGMDTO;
import com.trackingpath.dtos.RoutesDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.RouteService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

	private final RouteService routeService;
	private final AuthenticationService authenticationService;
	@Autowired
	private ActivityLogService activityLogService;

	// CREATE
	@PostMapping
	public ResponseEntity<?> createRoute(@RequestBody RoutesDTO bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean ok = routeService.createRoutes(bean, user.getId(), user.getAdminId());
		activityLogService.createActivity("NEW ROUTE CREATED",
				"ROUTE(" + bean.getName() + ") CREATED BY " + user.getUsername(), user, request);	

		return ResponseEntity.ok(ok ? "Route created successfully" : "Failed!");
	}

	// READ ALL
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllRoutes(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(defaultValue = "") String search) {

	    Map<String, Object> response =
	            routeService.getRoutes(page, size, search);

	    return ResponseEntity.ok(response);
	}


	// READ BY ID
	@GetMapping("/{id}")
	public Map<String, Object> getRouteById(@PathVariable long id) {
		return routeService.getRouteById(id);
	}

	// DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteRoute(@PathVariable long id,HttpServletRequest request) {
		Users user = authenticationService.getUser();
		  boolean ok = routeService.deleteRoutes(id);
		    if (ok) {
		        activityLogService.createActivity(
		                "ROUTE_DELETED",
		                "Route deleted with ID (" + id + ") by " + user.getUsername(),
		                user,
		                request
		        );
		    } else {
		        activityLogService.createActivity(
		                "ROUTE_DELETE_FAILED",
		                "Failed to delete route with ID (" + id + ") by " + user.getUsername(),
		                user,
		                request
		        );
		    }
		return ResponseEntity.ok(ok ? "Route deleted successfully" : "Failed!");
	}

	// UPDATE
	@PutMapping
	public ResponseEntity<?> updateRoute(@RequestBody RoutesDTO bean,HttpServletRequest request) {
		Users user = authenticationService.getUser();

		boolean updated = routeService.updateRoute(bean);
		if (updated) {
	        activityLogService.createActivity(
	                "ROUTE_UPDATED",
	                "ROUTE (" + bean.getName() + ") UPDATED BY " + user.getUsername(),
	                user,
	                request
	        );
	    }
		return updated ? ResponseEntity.ok("Route updated successfully")
				: ResponseEntity.badRequest().body("Update failed");
	}

	// ROUTE GROUPS
	@PostMapping(value = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String addRouteGroupData(@RequestBody DGMDTO bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean anyFailed = false;

		// Add new groups
		if (bean.getGroup_names() != null) {
			for (String name : bean.getGroup_names()) {
				if (name != null && !name.trim().isEmpty()) {
					activityLogService.createActivity(
	                        "NEW ROUTE GROUP CREATED",
	                        "ROUTE GROUP (" + name + ") CREATED BY " + user.getUsername(),
	                        user,
	                        request
	                    );
					if (!routeService.addDataRouteGroup(name.trim(), user))
						anyFailed = true;
				}
			}
		}

		// Delete removed groups
		if (bean.getDeletedIds() != null) {
			for (Integer id : bean.getDeletedIds()) {
				activityLogService.createActivity(
	                    "ROUTE GROUP DELETED",
	                    "ROUTE GROUP (ID: " + id + ") DELETED BY " + user.getUsername(),
	                    user,
	                    request
	                );
				if (!routeService.deleteRouteGroup(id, user))
					anyFailed = true;
			}
		}

		return anyFailed ? "Some group operations failed." : "Groups saved successfully.";
	}

	@GetMapping("/groups")
	public @ResponseBody Map<Integer, String> getAllPoiGroupName() {

		Users user = authenticationService.getUser();
		return routeService.getAllRouteGroupName(user);
	}
	   
    @GetMapping("/all")
    public Map<Long, String> getAllRoute() {
        return routeService.getAllRoute();
    }
}
