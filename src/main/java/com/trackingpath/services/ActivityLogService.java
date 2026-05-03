package com.trackingpath.services;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.ActivityLogDTO;
import com.trackingpath.entities.ActivityLog;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.ActivityLogRepository;
import com.trackingpath.repositories.ModuleLogTypeMappingRepository;
import com.trackingpath.util.DateTimeHelper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

	private final ModuleLogTypeMappingRepository repository;
	private final ActivityLogRepository logrepository;

  

    public Map<Long, String> getAllModules(Users user) {
        // user is kept for compatibility / future filtering
        List<Object[]> result = repository.findAllModules();

        Map<Long, String> moduleMap = new LinkedHashMap<>();
        for (Object[] row : result) {
            moduleMap.put((Long) row[0], (String) row[1]);
        }

        return moduleMap;
    }
    public List<String> getLogTypesByModule(String module) {
        return repository.findLogTypesByModule(module);
    }
    public List<ActivityLogDTO> getActivityLog(
            Users user,
            String from,
            String to,
            String module,
            String logType
    ) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDateTime fromTime = LocalDate.parse(from.trim(), formatter).atStartOfDay();
        LocalDateTime toTime   = LocalDate.parse(to.trim(), formatter).atTime(LocalTime.MAX);

        List<String> logTypes = null;
        if (logType != null) {
            List<String> temp = Arrays.stream(logType.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (!temp.isEmpty()) {
                logTypes = temp;
                module = null; // 🔥 IMPORTANT: disable module filter
            }
        }
        List<ActivityLog> logs = logrepository.findAll(
                
                        user.getAdminId(),
                        fromTime,
                        toTime,
                        logTypes,
                        module  );

        return logs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private ActivityLogDTO toDto(ActivityLog log) {

        return ActivityLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .createdBy(log.getCreatedBy())
                .activityType(log.getLogType())
                .message(log.getMessage())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .httpReferal(log.getHttpReferal())
                .activityTime(formatTimestamp(
                                Timestamp.valueOf(log.getCreationTime()),
                                "yyyy-MM-dd HH:mm:ss",
                                "Asia/Kolkata"
                        )
                )
                .build();
    }
    public static String formatTimestamp(Timestamp ts, String pattern, String zone) {
	    return ts.toInstant()
	             .atZone(ZoneId.of(zone))
	             .format(DateTimeFormatter.ofPattern(pattern));
	}
    public boolean createActivity(
            String activityType,
            String message,
            Users user,
            HttpServletRequest request
    ) {
        try {
            ActivityLog log = ActivityLog.builder()
                    .creationTime(LocalDateTime.now())
                    .logType(activityType)
                    .message(message)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .httpReferal(request.getHeader("Referer"))
                    .createdBy(String.valueOf(user.getAdminId()))
                    .userId(user.getId())
                    .build();

            logrepository.save(log);
            return true;
        } catch (Exception e) {
            // optional: log error
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
    
}
