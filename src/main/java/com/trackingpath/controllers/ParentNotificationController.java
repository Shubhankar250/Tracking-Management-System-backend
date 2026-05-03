package com.trackingpath.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trackingpath.dtos.ApiResponse;
import com.trackingpath.dtos.*;
import com.trackingpath.services.ParentNotificationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parent")
public class ParentNotificationController {

    private final ParentNotificationService parentNotificationService;

    public ParentNotificationController(ParentNotificationService parentNotificationService) {
        this.parentNotificationService = parentNotificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(@RequestParam(required = false) Long passengerId,
                                                                                  @RequestParam(defaultValue = "0") Integer page,
                                                                                  @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(ApiResponse.ok(parentNotificationService.getNotifications(passengerId, page, size)));
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<ParentNotificationDto>> getNotification(@PathVariable Long notificationId) {
        return ResponseEntity.ok(ApiResponse.ok(parentNotificationService.getNotification(notificationId)));
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<ApiResponse<Long>> markRead(@Valid @RequestBody MarkNotificationsReadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications marked as read", parentNotificationService.markRead(request)));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        parentNotificationService.markAllRead();
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    @GetMapping("/preferences/notifications")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences() {
        return ResponseEntity.ok(ApiResponse.ok(parentNotificationService.getPreferences()));
    }

    @PostMapping("/preferences/notifications")
    public ResponseEntity<ApiResponse<Void>> savePreferences(@RequestBody NotificationPreferenceRequest request) {
        parentNotificationService.savePreferences(request);
        return ResponseEntity.ok(ApiResponse.ok("Notification preferences updated successfully", null));
    }
}
