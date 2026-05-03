package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.AddGeoGroupDTO;
import com.trackingpath.dtos.GeofenceDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.GeofenceService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/geofences")
public class GeofenceController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private GeofenceService geoService;
    @Autowired
    private ActivityLogService activityLogService;

    /* =========================
       Geofences CRUD
    ========================= */

    // GET /geofences
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGeofences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        Users user = authenticationService.getCurrentUser();
        Map<String, Object> response = geoService.getGeofence(user, page, size, search);

        return ResponseEntity.ok(response);
    }

    // POST /geofences
    @PostMapping
    public ResponseEntity<String> createGeofence(@RequestBody GeofenceDTO geo,HttpServletRequest request) {
        Users user = authenticationService.getCurrentUser();
        activityLogService.createActivity("NEW GEOFENCE CREATED",
				"GEOFENCE(" + geo.getPcts_name() + ") CREATED BY " + user.getUsername(), user, request);
        return geoService.createGeofence(geo, user)
                ? ResponseEntity.ok("Geofence added successfully")
                : ResponseEntity.badRequest().body("Failed");
    }

    // PUT /geofences/{id}
    @PutMapping("/{id}")
    public ResponseEntity<String> updateGeofence(
            @PathVariable Long id,
            @RequestBody GeofenceDTO newGeofence,
            HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();

        if (geoService.updateGeofencing(id, newGeofence)) {

            activityLogService.createActivity(
                    "GEOFENCE_UPDATED",
                    "GEOFENCE (" + newGeofence.getPcts_name() + ") UPDATED BY " + user.getUsername(),
                    user,
                    request
            );

            return ResponseEntity.ok("Geofence updated successfully");
        }

        return ResponseEntity.badRequest().body("Failed");
    }


    // DELETE /geofences/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGeofence(@PathVariable Long id, HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();

        if (geoService.deleteGeofence(id)) {

            activityLogService.createActivity(
                    "GEOFENCE_DELETED",
                    "GEOFENCE DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(),
                    user,
                    request
            );

            return ResponseEntity.ok("Geofence deleted");
        }

        return ResponseEntity.badRequest().body("Failed");
    }


    /* =========================
       Geo Groups
    ========================= */

    // GET /geofences/geogroups
    @GetMapping("/geogroups")
    public Map<Long, String> getAllGeoGroups() {
        Users user = authenticationService.getCurrentUser();
        return geoService.getAllGeoGroupName(user);
    }

    // POST /geofences/geogroups
    @PostMapping("/geogroups")
    public ResponseEntity<String> saveGeoGroups(
            @RequestBody AddGeoGroupDTO group,
            HttpServletRequest request
    ) {

        Users user = authenticationService.getCurrentUser();
        boolean anyFailed = false;

        if (group.getGroup_names() != null) {
            for (String name : group.getGroup_names()) {
                if (name != null && !name.trim().isEmpty()) {
                    if (!geoService.addDatageoGroup(name.trim(), user)) {

                        activityLogService.createActivity(
                                "NEW GEOFENCE GROUP CREATED",
                                "GEOFENCE GROUP (" + name + ") CREATED BY " + user.getUsername(),
                                user,
                                request
                        );

                        anyFailed = true;
                    }
                }
            }
        }

        if (group.getDeletedIds() != null) {
            for (Long id : group.getDeletedIds()) {
                if (!geoService.deleteGeoGroup(id, user)) {

                    activityLogService.createActivity(
                            "GEOFENCE GROUP DELETED",
                            "GEOFENCE GROUP (ID: " + id + ") DELETED BY " + user.getUsername(),
                            user,
                            request
                    );

                    anyFailed = true;
                }
            }
        }

        return anyFailed
                ? ResponseEntity.badRequest().body("Failed")
                : ResponseEntity.ok("Groups saved successfully");
    }


    /* =========================
       Global
    ========================= */

    // GET /geofences/all
    @GetMapping("/all")
    public Map<Long, String> getAllGeofencesGlobal() {
        return geoService.getAllGeofence();
    }
}
