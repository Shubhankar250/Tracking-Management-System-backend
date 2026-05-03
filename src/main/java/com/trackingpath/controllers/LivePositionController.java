package com.trackingpath.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.util.DateTimeHelper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/positions")
public class LivePositionController {
	@Autowired
	AuthenticationService authenticationService;

    private final SimpMessageSendingOperations messaging;
    
	@Autowired
	private DeviceRepository deviceRepository;

    public LivePositionController(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }

    @PostMapping
    public ResponseEntity<?> push(@RequestBody Map<String, Object> data) {
    	  Users user = authenticationService.getCurrentUser();
    	  System.out.println("User Timezone is "+user.getTimezone());

        long deviceId = Long.parseLong(data.get("deviceId").toString());
        
        String devicetimezone = deviceRepository.findDeviceTimeZone(deviceId);
        
        System.out.println("Device time zone is "+devicetimezone);
        
        List<String> users = List.of("admin", "user1");
        LocalDateTime deviceTime = LocalDateTime.parse(data.get("devicetime").toString());
        System.out.println("Device time is "+deviceTime);
        
        LocalDateTime convertedTime= DateTimeHelper.convertToUserZone(
        		deviceTime,
                devicetimezone,
                user.getTimezone()
            );

        for (String u : users) {
        	
        	Map<String, Object> updatedData = new HashMap<>(data);
            updatedData.put("devicetime", convertedTime.toString());
            messaging.convertAndSendToUser(
                u,
                "/queue/device." + deviceId,
                data
            );
        }

        return ResponseEntity.ok("sent");
    }
}
