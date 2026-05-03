package com.trackingpath.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.NotificationDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.EventsRepository;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class EventNotificationService {
	private final EventsRepository eventsRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public List<NotificationDTO> getNotifications(
	        Users user,
	        String stime,
	        String etime,
	        String alertType,
	        String deviceIds) {

	    ZoneId userZone = ZoneId.of(user.getTimezone());

	    ZonedDateTime userStart;
	    ZonedDateTime userEnd;

	    // ✅ default: today full day (user timezone)
	    if (stime == null || stime.isBlank() || etime == null || etime.isBlank()) {

	        LocalDate today = LocalDate.now(userZone);

	        userStart = today.atStartOfDay(userZone);
	        userEnd = today.atTime(23, 59, 59).atZone(userZone);

	    } else {


	        userStart = LocalDateTime.parse(stime, formatter).atZone(userZone);
	        userEnd = LocalDateTime.parse(etime, formatter).atZone(userZone);
	    }

	    // ✅ convert to UTC (ONLY ONCE)
	    LocalDateTime startUTC = userStart.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	    LocalDateTime endUTC = userEnd.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

String startStr = startUTC.format(formatter);
String endStr   = endUTC.format(formatter);
	    Long userId = null;
	    Long adminId = null;

	    if (user.getRoles().stream()
	            .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
	        adminId = user.getAdminId();
	    } else {
	        userId = user.getId();
	    }

	    String[] alertTypes = (alertType != null && !alertType.isBlank())
	            ? Arrays.stream(alertType.split(",")).map(String::trim).toArray(String[]::new)
	            : null;

	    Long[] deviceIdArray = (deviceIds != null && !deviceIds.isBlank())
	            ? Arrays.stream(deviceIds.split(",")).map(Long::valueOf).toArray(Long[]::new)
	            : null;

	    return eventsRepository.fetchAlertNotifications(
	    		startStr,
	    		endStr,
	            userId,
	            adminId,
	            alertTypes,
	            deviceIdArray
	    )
	    .stream()
	    .map(r -> mapToDTO(r, user))
	    .toList();
	}

	private NotificationDTO mapToDTO(Object[] r, Users user) {

	    NotificationDTO dto = new NotificationDTO();

	    dto.setDeviceName((String) r[0]);
	    dto.setStatus((String) r[1]);
	    dto.setAttributes((String) r[3]);

	    dto.setCourse(r[4] != null ? ((Number) r[4]).doubleValue() : null);
	    dto.setAltitude(r[5] != null ? ((Number) r[5]).doubleValue() : null);
	    dto.setSpeed(r[6] != null ? ((Number) r[6]).doubleValue() : null);

	    long epochSeconds = ((Number) r[7]).longValue();

	    LocalDateTime utcDateTime = Instant.ofEpochSecond(epochSeconds)
	            .atZone(ZoneOffset.UTC)
	            .toLocalDateTime();

	    dto.setAlertTime(
	            DateTimeHelper.convertUtcToUser(
	                    utcDateTime,
	                    user.getTimezone()
	            )
	    );

	    dto.setAlertType((String) r[8]);

	    dto.setLatitude(r[9] != null ? ((Number) r[9]).doubleValue() : null);
	    dto.setLongitude(r[10] != null ? ((Number) r[10]).doubleValue() : null);

	    dto.setAddress((String) r[11]);
	    dto.setMessage((String) r[12]);

	    dto.setDeviceId(r[13] != null ? ((Number) r[13]).longValue() : null);
	    dto.setUserId(r[14] != null ? ((Number) r[14]).longValue() : null);

	    return dto;
	}
	
	
	
	public List<NotificationDTO> getAlertNotificationPopUp(
	        Users user,
	        String start,
	        String end
	) {

	    // ✅ ADD THIS
	    start = DateTimeUtil.convertUserTimeToUTC(start, user.getTimezone());
	    end   = DateTimeUtil.convertUserTimeToUTC(end, user.getTimezone());

	    Long userId = null;
	    Long adminId = null;

	    if (user.getRoles().stream()
	            .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
	        adminId = user.getAdminId();
	    } else {
	        userId = user.getId();
	    }

	    return eventsRepository.getAlertNotificationPopUp(
	            start, end, userId, adminId
	    )
	    .stream()
	    .map(r -> mapToDTOPopUp(r, user))
	    .toList();
	}
	    private NotificationDTO mapToDTOPopUp(Object[] r,Users user) {
	        NotificationDTO dto = new NotificationDTO();

	        dto.setDeviceName((String) r[0]);
	        dto.setStatus((String) r[1]);
	    
	        long epochSeconds = ((Number) r[2]).longValue();
	        LocalDateTime utcDateTime = Instant
	                .ofEpochSecond(epochSeconds)
	                .atZone(ZoneOffset.UTC)
	                .toLocalDateTime();
	        dto.setAlertTime(
	        		   DateTimeHelper.convertUtcToUser(
	   	                    utcDateTime,
	   	                    user.getTimezone()
	   	            )
	        	);
	        dto.setAlertType((String) r[3]);
	        dto.setLatitude((Double) r[4]);
	        dto.setLongitude((Double) r[5]);
	        dto.setAddress((String) r[6]);
	        dto.setMessage((String) r[7]);
	        dto.setDeviceId(((Number) r[8]).longValue());
	        dto.setUserId(((Number) r[9]).longValue());
	
	        dto.setSoundNotification((String) r[10]);
	        dto.setPopupNotification((String) r[11]);
	        dto.setNotificationColor((String) r[12]);
	        dto.setSpeed((Double) r[13]);

	        return dto;
	    }

	 
}


