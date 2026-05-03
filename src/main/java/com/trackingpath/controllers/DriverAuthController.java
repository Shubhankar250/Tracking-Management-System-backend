package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.services.DriverAuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/driver/auth")
public class DriverAuthController {

    private final DriverAuthService driverAuthService;

    public DriverAuthController(DriverAuthService driverAuthService) {
        this.driverAuthService = driverAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<DriverLoginResponse>> login(@Valid @RequestBody DriverLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", driverAuthService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverMeResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(driverAuthService.me()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
    	driverAuthService.logout();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    	driverAuthService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    	driverAuthService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset instructions sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    	driverAuthService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful", null));
    }
}
