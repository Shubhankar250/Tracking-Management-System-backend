package com.trackingpath.controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.EmergencyAlertRequest;
import com.trackingpath.dtos.MarkAbsenceRequest;
import com.trackingpath.dtos.SupportMessageRequest;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.services.ParentActionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parent")
public class ParentActionController {

    private final ParentActionService parentActionService;

    public ParentActionController(ParentActionService parentActionService) {
        this.parentActionService = parentActionService;
    }

    @PostMapping("/absence")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAbsence(@Valid @RequestBody MarkAbsenceRequest request) {
        Long id = parentActionService.markAbsence(request);
        return ResponseEntity.ok(ApiResponse.ok("Absence marked successfully for selected trip", Map.of("absenceRequestId", id)));
    }

    @PostMapping("/support/message")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendSupportMessage(@Valid @RequestBody SupportMessageRequest request) {
        Long id = parentActionService.sendSupportMessage(request);
        return ResponseEntity.ok(ApiResponse.ok("Your note has been sent to the transport team", Map.of("ticketId", id)));
    }

    @PostMapping("/emergency-alert")
    public ResponseEntity<ApiResponse<Map<String, Object>>> emergencyAlert(@Valid @RequestBody EmergencyAlertRequest request) {
        Long id = parentActionService.raiseEmergencyAlert(request);
        return ResponseEntity.ok(ApiResponse.ok("Emergency alert raised successfully", Map.of("alertId", id)));
    }
}
