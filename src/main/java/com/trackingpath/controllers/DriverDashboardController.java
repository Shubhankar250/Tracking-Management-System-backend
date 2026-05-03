package com.trackingpath.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.dtos.BreakdownDetailsDto;
import com.trackingpath.dtos.DriverDashboardResponse;
import com.trackingpath.dtos.DriverLiveTripDto;
import com.trackingpath.dtos.PassengerListDto;
import com.trackingpath.dtos.StopListDto;
import com.trackingpath.dtos.VehicleEventRequest;
import com.trackingpath.entities.StaffDetails;
import com.trackingpath.entities.TripVehicleEvent;
import com.trackingpath.services.DriverDashboardService;


@RestController
@RequestMapping("/api/driver")
public class DriverDashboardController {

    private final DriverDashboardService driverDashboardService;

    public DriverDashboardController(DriverDashboardService driverDashboardService) {
        this.driverDashboardService = driverDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DriverDashboardResponse>> getDashboard(@RequestParam Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverDashboardService.getDashboard(driverId)));
    }
    @GetMapping("/stops")
    public ResponseEntity<ApiResponse<List<StopListDto>>> getStops(@RequestParam Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverDashboardService.getStopList(driverId)));
    }
    @GetMapping("/live-trip")
    public ResponseEntity<ApiResponse<DriverLiveTripDto>> getLiveTrip(@RequestParam Long driverId) {
        return ResponseEntity.ok(ApiResponse.ok(driverDashboardService.getLiveTrip(driverId)));
    }
    @GetMapping("/passengers")
    public ResponseEntity<ApiResponse<List<PassengerListDto>>> getPassengers(
            @RequestParam Long driverId,
            @RequestParam(required = false) String pickupStop,
            @RequestParam(required = false) String attendanceStatus) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        driverDashboardService.getPassengerList(driverId, pickupStop, attendanceStatus)
                )
        );
    }
    @GetMapping("/staffs")
    public ResponseEntity<ApiResponse<List<StaffDetails>>> getAllStaffs() {
        return ResponseEntity.ok(
                ApiResponse.ok(driverDashboardService.getAllStaffs())
        );
    }
    @PutMapping("/passengers/mark-all-boarded")
    public ResponseEntity<ApiResponse<String>> markAllBoarded(
            @RequestParam Long driverId,
            @RequestParam(required = false) String pickupStop,
            @RequestParam(required = false) String attendanceStatus) {

        driverDashboardService.markAllBoarded(driverId, pickupStop, attendanceStatus);
        return ResponseEntity.ok(ApiResponse.ok("All marked as BOARDED"));
    }

    @PutMapping("/passengers/mark-all-deboarded")
    public ResponseEntity<ApiResponse<String>> markAllDeboarded(
            @RequestParam Long driverId,
            @RequestParam(required = false) String pickupStop,
            @RequestParam(required = false) String attendanceStatus) {

        driverDashboardService.markAllDeboarded(driverId, pickupStop, attendanceStatus);
        return ResponseEntity.ok(ApiResponse.ok("All marked as DEBOARDED"));
    }

    @PutMapping("/passengers/mark-all-absent")
    public ResponseEntity<ApiResponse<String>> markAllAbsent(
            @RequestParam Long driverId,
            @RequestParam(required = false) String pickupStop,
            @RequestParam(required = false) String attendanceStatus) {

        driverDashboardService.markAllAbsent(driverId, pickupStop, attendanceStatus);
        return ResponseEntity.ok(ApiResponse.ok("All marked as ABSENT"));
    }
    @PutMapping("/passenger/{id}/mark-boarded")
    public ResponseEntity<ApiResponse<String>> markBoarded(
            @PathVariable Long id) {

        driverDashboardService.markPassengerBoarded(id);
        return ResponseEntity.ok(ApiResponse.ok("Passenger marked PRESENT & BOARDED"));
    }

    @PutMapping("/passenger/{id}/mark-absent")
    public ResponseEntity<ApiResponse<String>> markAbsent(
            @PathVariable Long id) {

        driverDashboardService.markPassengerAbsent(id);
        return ResponseEntity.ok(ApiResponse.ok("Passenger marked ABSENT"));
    }
    @PostMapping("/vehicle/breakdown")
    public ResponseEntity<ApiResponse<String>> reportBreakdown(
            @RequestBody VehicleEventRequest request) {

        driverDashboardService.reportBreakdown(request);

        return ResponseEntity.ok(ApiResponse.ok("Breakdown reported successfully"));
    }
    @PostMapping("/vehicle/replace")
    public ResponseEntity<ApiResponse<String>> replaceVehicle(
            @RequestBody VehicleEventRequest request) {

        driverDashboardService.replaceVehicle(request);

        return ResponseEntity.ok(ApiResponse.ok("Vehicle replaced successfully"));
    }
    @GetMapping("/breakdown-details")
    public ResponseEntity<ApiResponse<BreakdownDetailsDto>> getBreakdownDetails(
            @RequestParam Long driverId) {

        BreakdownDetailsDto response =
                driverDashboardService.getBreakdownDetails(driverId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Breakdown details fetched", response)
        );
    }
    @GetMapping("/vehicle-events")
    public ResponseEntity<ApiResponse<List<TripVehicleEvent>>> getVehicleEventsByDriver(
            @RequestParam Long driverId) {

        List<TripVehicleEvent> events =
                driverDashboardService.getVehicleEventsByDriver(driverId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Driver trip vehicle events fetched", events)
        );
    }
}
