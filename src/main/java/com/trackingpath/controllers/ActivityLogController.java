package com.trackingpath.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.ActivityLogDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityLogController {
	
   

    @Autowired
	AuthenticationService authenticationService;
    private final ActivityLogService activityLogService;
    @GetMapping("/allModules")
    public Map<Long, String> getAllModule(HttpServletRequest request) {
    	Users user = authenticationService.getCurrentUser();
        return activityLogService.getAllModules(user);
    }
    @GetMapping("/logTypesByModule")
    public List<String> getLogTypesByModule(
            @RequestParam("module") String module) {

        return activityLogService.getLogTypesByModule(module);
    }
    @GetMapping("/activitylogs")
    public List<ActivityLogDTO> getActivityLog(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String module,
            @RequestParam(required = false, name = "log_type") String logType
    ) {
        Users user = authenticationService.getCurrentUser();
        return activityLogService.getActivityLog(user, from, to, module, logType);
    }
    
}
