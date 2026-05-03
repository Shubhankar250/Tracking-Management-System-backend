package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.services.ParentAuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parent/auth")
public class ParentAuthController {

    private final ParentAuthService parentAuthService;

    public ParentAuthController(ParentAuthService parentAuthService) {
        this.parentAuthService = parentAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ParentLoginResponse>> login(@Valid @RequestBody ParentLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", parentAuthService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ParentMeResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(parentAuthService.me()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        parentAuthService.logout();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        parentAuthService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        parentAuthService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset instructions sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        parentAuthService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful", null));
    }
}
