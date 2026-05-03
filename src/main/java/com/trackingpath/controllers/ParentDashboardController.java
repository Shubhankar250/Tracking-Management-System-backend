package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.dtos.ParentChildrenResponse;
import com.trackingpath.dtos.ParentDashboardResponse;
import com.trackingpath.dtos.RouteLiveDataResponse;
import com.trackingpath.services.ParentDashboardService;

@RestController
@RequestMapping("/api/parent")
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;

    public ParentDashboardController(ParentDashboardService parentDashboardService) {
        this.parentDashboardService = parentDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ParentDashboardResponse>> getDashboard(@RequestParam Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(parentDashboardService.getDashboard(passengerId)));
    }

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<ParentChildrenResponse>> getChildren() {
        return ResponseEntity.ok(ApiResponse.ok(parentDashboardService.getChildren()));
    }
    
    
    @GetMapping("/livedata")
    public ResponseEntity<ApiResponse<RouteLiveDataResponse>> getLiveData() {
        return ResponseEntity.ok(ApiResponse.ok(parentDashboardService.getLiveData()));
    }
    
    
    
    
}
