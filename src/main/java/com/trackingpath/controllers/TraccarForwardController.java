package com.trackingpath.controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.trackingpath.configs.UploadConfig;
import com.trackingpath.dtos.DeviceInfo;
import com.trackingpath.dtos.PositionUpdate;
import com.trackingpath.services.DeviceService;

@RestController
@RequestMapping("/traccar")
public class TraccarForwardController {

    private final SoftwareReleaseController softwareReleaseController;

	
	private final DeviceService deviceService;

	private static final String TOKEN = "7483926150";

	private final SimpMessageSendingOperations stomp;

	public TraccarForwardController(SimpMessageSendingOperations stomp, DeviceService deviceService, SoftwareReleaseController softwareReleaseController) {
		this.stomp = stomp;
		this.deviceService = deviceService;
		this.softwareReleaseController = softwareReleaseController;
	}

	@PostMapping("/positions")
	public ResponseEntity<?> receive(@RequestHeader("X-TP-Token") String token, @RequestBody Map<String, Object> body) {
		if (!TOKEN.equals(token)) {
			return ResponseEntity.status(401).body("Invalid token");
		}

		List<Map<String, Object>> positions;

		if (body.containsKey("positions")) {
			positions = (List<Map<String, Object>>) body.get("positions");
		} else {
			positions = List.of(body);
		}

		for (Map<String, Object> p : positions) {
			PositionUpdate msg = mapToDto(p);
			if (msg != null) {
			    stomp.convertAndSend("/topic/positions", msg);
			}
		}

		return ResponseEntity.ok("OK");
	}
	private PositionUpdate mapToDto(Map<String, Object> p) {

	    try {
	        long deviceId = ((Number) p.get("deviceId")).longValue();

	        DeviceInfo info = deviceService.getDeviceById(deviceId);

	        PositionUpdate dto = new PositionUpdate();

	        dto.setDeviceId(deviceId);
	        dto.setDeviceUniqueId(String.valueOf(p.get("deviceUniqueId")));

	        dto.setLatitude(round6(((Number) p.get("latitude")).doubleValue()));
	        dto.setLongitude(round6(((Number) p.get("longitude")).doubleValue()));
	        dto.setSpeed(round1(((Number) p.getOrDefault("speed", 0)).doubleValue()));
	        dto.setCourse(round1(((Number) p.getOrDefault("course", 0)).doubleValue()));

	        dto.setValid(Boolean.TRUE.equals(p.get("valid")));
 
	     // ---- FIX TIME ----
	        dto.setFixTime(convertToUtcIfNeeded(
	                p.get("fixTime"),
	                info.getDevicetimezone()
	        ));

	        // ---- DEVICE TIME ----
	        dto.setDevicetime(convertToUtcIfNeeded(
	                p.get("deviceTime"),
	                info.getDevicetimezone()
	        ));
	        dto.setServertime(convertToUtcIfNeeded(
	                p.get("serverTime"),
	                info.getDevicetimezone()
	        ));
	        // ---- DEVICE TIME ----
	     
	        dto.setAltitude(((Number) p.getOrDefault("altitude", 0)).doubleValue());
	        dto.setAddress((String) p.get("address"));

	        // ---- ATTRIBUTES ----
	        Map<String, Object> attributes =
	            (Map<String, Object>) p.getOrDefault("attributes", Map.of());

	        dto.setAttributes(new LinkedHashMap<>(attributes));

	        Object ig = attributes.get("ignition");
	        dto.setIgnition(ig == null ? null : Boolean.valueOf(ig.toString()));

	        // ---- DEVICE INFO ----
	        dto.setLastidletime(info.getLastidletime());
	        dto.setLastmovementtime(info.getLastmovementtime());
	        Long deviceTime = dto.getDevicetime();

	        String status = "Offline";

	        if (deviceTime != null && deviceTime > 0) {

	            long currentMillis = Instant.now().toEpochMilli();

	            long diffSeconds = (currentMillis - deviceTime) / 1000;

	            if (diffSeconds <= 300) {
	                status = "online";
	            }
	        }

	        dto.setStatus(status);	        
	        dto.setImgIconName(info.getImgIconName());
	        dto.setIconType(info.getIconType());
	        dto.setName(info.getName());
	        dto.setDevicetimezone(info.getDevicetimezone());

	        return dto;

	    } catch (Exception e) {
	        System.err.println("Drop bad position: " + p + " cause=" + e);
	        return null;
	    }
	}
	private long convertToUtcIfNeeded(Object timeObj, String deviceTimezone) {

	    if (timeObj == null) {
	        return 0L;
	    }

	    if (deviceTimezone == null
	            || deviceTimezone.isBlank()
	            || "UTC".equalsIgnoreCase(deviceTimezone)
	            || "Etc/UTC".equalsIgnoreCase(deviceTimezone)) {

	        return (timeObj instanceof Number)
	                ? ((Number) timeObj).longValue()
	                : Instant.parse(timeObj.toString()).toEpochMilli();
	    }

	    ZoneId deviceZone = ZoneId.of(deviceTimezone);

	    LocalDateTime localDateTime;

	    if (timeObj instanceof Number) {
	        long millis = ((Number) timeObj).longValue();

	        // millis ko local clock time ki tarah treat karo
	        localDateTime = Instant.ofEpochMilli(millis)
	                .atZone(ZoneOffset.UTC)
	                .toLocalDateTime();
	    } else {
	        String str = timeObj.toString().replace("T", " ");

	        if (str.length() >= 19) {
	            str = str.substring(0, 19);
	        }

	        localDateTime = LocalDateTime.parse(
	                str,
	                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	        );
	    }

	    return localDateTime
	            .atZone(deviceZone)
	            .withZoneSameInstant(ZoneOffset.UTC)
	            .toInstant()
	            .toEpochMilli();
	}
	private static double round6(double value) {
	    return Math.round(value * 1_000_000d) / 1_000_000d;
	}

	private static double round1(double value) {
	    return Math.round(value * 10d) / 10d;
	}
}
