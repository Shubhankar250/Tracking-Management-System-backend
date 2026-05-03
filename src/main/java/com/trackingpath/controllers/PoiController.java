package com.trackingpath.controllers;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.trackingpath.dtos.PoiDto;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.PoiService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/poi")
public class PoiController {

    private final PoiService poiService;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ActivityLogService activityLogService;

    private final ResourceLoader resourceLoader;
    private final ServletContext servletContext;



    public PoiController(PoiService poiService,
                         AuthenticationService authenticationService,
                         ResourceLoader resourceLoader,
                         ServletContext servletContext) {
        this.poiService = poiService;
        this.authenticationService = authenticationService;
        this.resourceLoader = resourceLoader;
        this.servletContext = servletContext;
    }


    // CREATE POI
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody PoiDto createPoi(@RequestBody PoiDto poi,HttpServletRequest req) {
        Users user = authenticationService.getUser();
        activityLogService.createActivity("NEW POI CREATED",
				"POI(" + poi.getName() + ") CREATED BY " + user.getUsername(), user, req);
        return poiService.createPoi(poi, user);
    }

    // READ ALL POI (JSON)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPoiData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        Users user = authenticationService.getUser();
        Map<String, Object> response =
                poiService.getAllPois(user, page, size, search);

        return ResponseEntity.ok(response);
    }


   
    @GetMapping("/{id}")
    public @ResponseBody PoiDto getPoiById(@PathVariable Long id) {

        Users user = authenticationService.getUser();

        return poiService.getPoiById(id, user)
                .orElseThrow(() -> new RuntimeException("POI not found"));
    }


    // DELETE POI
    @DeleteMapping("/{id}")
    public @ResponseBody String deletePoi(@PathVariable Long id ,HttpServletRequest req) {

        Users user = authenticationService.getUser();
        boolean ok = poiService.deletePoi(id, user);
        activityLogService.createActivity("POI DELETED",
				"POI DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, req);	
        return ok ? "POI deleted successfully!" : "failed!";
    }

    // UPDATE POI
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String updatePoi(@RequestBody PoiDto poi,HttpServletRequest req) {

        Users user = authenticationService.getUser();
        boolean status = poiService.updatePoi(poi, user);
        activityLogService.createActivity("POI UPDATED",
				"POI(" + poi.getName() + ") UPDATED BY " + user.getUsername(), user, req);

        return status ? "Poi Details updated successfully!" : "Failed!";
    }



    // POI GROUPS
    @GetMapping("/groups")
    public @ResponseBody Map<Integer, String> getAllPoiGroupName() {

        Users user = authenticationService.getUser();
        return poiService.getAllPoiGroupName(user);
    }

    // SAVE POI GROUPS
    @PostMapping(value = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String addPoiGroupData(@RequestBody GroupCreateRequest request, HttpServletRequest req) {

        Users user = authenticationService.getUser();
        boolean anyFailed = false;

        if (request.getGroupNames() != null) {
            for (String g : request.getGroupNames()) {
                if (g != null && !g.trim().isEmpty()) {
                	activityLogService.createActivity(
 	                        "NEW POI GROUP CREATED",
 	                        "POI GROUP (" + g + ") CREATED BY " + user.getUsername(),
 	                        user,
 	                       req
 	                    );
                    if (!poiService.addPoiGroup(g.trim(), user)) anyFailed = true;
                }
            }
        }

        if (request.getDeletedIds() != null) {
            for (Integer id : request.getDeletedIds()) {
            	activityLogService.createActivity(
	                    "POI GROUP DELETED",
	                    "POI GROUP (ID: " + id + ") DELETED BY " + user.getUsername(),
	                    user,
	                    req
	                );
                if (!poiService.deletePoiGroup(id, user)) anyFailed = true;
            }
        }

        return anyFailed ? "Some group operations failed." : "Groups saved successfully.";
    }

    // DTO
    public static class GroupCreateRequest {
        private List<String> groupNames;
        private List<Integer> deletedIds;

        public List<String> getGroupNames() { return groupNames; }
        public void setGroupNames(List<String> groupNames) { this.groupNames = groupNames; }

        public List<Integer> getDeletedIds() { return deletedIds; }
        public void setDeletedIds(List<Integer> deletedIds) { this.deletedIds = deletedIds; }
    }
    
    
    
    @GetMapping("/all")
    public Map<Long, String> getAllPoi() {
        return poiService.getAllPoi();
    }
    
    
    
}
