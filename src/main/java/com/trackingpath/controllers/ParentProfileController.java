package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.dtos.ParentProfileResponse;
import com.trackingpath.dtos.UpdateParentProfileRequest;
import com.trackingpath.dtos.SupportContactsResponse;
import com.trackingpath.services.ParentProfileService;

@RestController
@RequestMapping("/api/parent")
public class ParentProfileController {

    private final ParentProfileService parentProfileService;

    public ParentProfileController(ParentProfileService parentProfileService) {
        this.parentProfileService = parentProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ParentProfileResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(parentProfileService.getProfile()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateParentProfileRequest request) {
        parentProfileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", null));
    }

    @GetMapping("/support/contacts")
    public ResponseEntity<ApiResponse<SupportContactsResponse>> getSupportContacts(@RequestParam Long passengerId) {
        return ResponseEntity.ok(ApiResponse.ok(parentProfileService.getSupportContacts(passengerId)));
    }
}
