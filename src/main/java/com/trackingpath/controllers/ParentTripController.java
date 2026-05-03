package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.dtos.*;
import com.trackingpath.services.ParentTripService;

@RestController
@RequestMapping("/api/parent")
public class ParentTripController {

    private final ParentTripService parentTripService;

    public ParentTripController(ParentTripService parentTripService) {
        this.parentTripService = parentTripService;
    }

    @GetMapping("/live-tracking")
    public ResponseEntity<ApiResponse<LiveTrackingResponse>> getLiveTracking(@RequestParam Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(parentTripService.getLiveTracking(passengerId)));
    }

    @GetMapping("/child-trip-status")
    public ResponseEntity<ApiResponse<ChildTripStatusResponse>> getChildTripStatus(@RequestParam Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(parentTripService.getChildTripStatus(passengerId)));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<TripScheduleResponse>> getSchedule(@RequestParam Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(parentTripService.getSchedule(passengerId)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<TripHistoryResponse>> getHistory(@RequestParam Long passengerId,
                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                       @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.ok(parentTripService.getHistory(passengerId, page, size)));
    }
}
