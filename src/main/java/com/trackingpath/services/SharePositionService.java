package com.trackingpath.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.APISuccessReponse;
import com.trackingpath.dtos.ExternalLiveDataDTO;
import com.trackingpath.dtos.SharePositionBean;
import com.trackingpath.dtos.SharePositionSheduleBean;
import com.trackingpath.entities.ApiManagerEntity;
import com.trackingpath.entities.SharePositionScheduleEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.GenericException;
import com.trackingpath.repositories.ApiManagerRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.LiveDataRepository;
import com.trackingpath.repositories.SharePositionScheduleRepository;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;
import com.trackingpath.util.TimeZoneConverter;
import org.springframework.http.HttpStatus;  // <-- Add this

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SharePositionService {
@Autowired
ApiManagerRepository apiRepo;
@Autowired
  SharePositionScheduleRepository scheduleRepo;
@Autowired
LiveDataRepository liveDataRepository;
@Autowired
DeviceRepository deviceRepo;

    public Long createSharePosition(SharePositionBean bean, Users user) {

    	 ApiManagerEntity.ApiManagerEntityBuilder builder = ApiManagerEntity.builder()
    	            .status(bean.isStatus())
    	            .name(bean.getName())
    	            .deviceId(bean.getDeviceId())
    	            .validTime(bean.getValidTime())
    	            .email(bean.getEmail())
    	            .phone(bean.getPhone())
    	            .baseUrl(bean.getBaseUrl())
    	            .deleteAfterExpiration(bean.isDeleteAfterExpiration())
    	            .accessCode(bean.getAccessCode())
    	            .userId(user.getId())
    	            .adminId(user.getAdminId());

    	    // ✅ Only include start/end time when NOT custom
    	    if (!"custom".equalsIgnoreCase(bean.getValidTime())) {
    	        builder
    	            .accessStartTime(bean.getAccessStartTime())
    	            .accessEndTime(bean.getAccessEndTime());
    	    }
    	    ApiManagerEntity entity = builder.build();


        apiRepo.save(entity);

        // Save custom schedule
        if ("custom".equalsIgnoreCase(bean.getValidTime())
                && bean.getSharePositionScheduleBean() != null) {

            SharePositionScheduleEntity schedule =
                    SharePositionScheduleEntity.builder()
                            .shareId(entity.getId())
                            .data(bean.getSharePositionScheduleBean().getData())
                            .adminId(user.getAdminId())
                            .userId(user.getId())
                            .build();

            scheduleRepo.save(schedule);
        }

        return entity.getId();
    }

    public void updateShare(SharePositionBean bean, Users user) {

        // 1️⃣ Fetch main share entity
        ApiManagerEntity entity = apiRepo.findById(bean.getId())
                .orElseThrow(() -> new RuntimeException("Share not found"));

        // 2️⃣ Update main fields
        entity.setName(bean.getName());
        entity.setDeviceId(bean.getDeviceId());
        entity.setStatus(bean.isStatus());
        entity.setValidTime(bean.getValidTime());
        entity.setEmail(bean.getEmail());
        entity.setPhone(bean.getPhone());
        entity.setDeleteAfterExpiration(bean.isDeleteAfterExpiration());
     // Only include start/end time when NOT custom
        if (!"custom".equalsIgnoreCase(bean.getValidTime())) {

            entity.setAccessStartTime(
                    bean.getAccessStartTime()
                   
            );

            entity.setAccessEndTime(
                bean.getAccessEndTime()
                
            );

        } else {
            entity.setAccessStartTime(null);
            entity.setAccessEndTime(null);
        }
        entity.setAccessCode(bean.getAccessCode());
        entity.setBaseUrl(bean.getBaseUrl());

        apiRepo.save(entity); // Save main entity
  
        // 3️⃣ Update schedule if exists
        if (bean.getSharePositionScheduleBean() != null) {
            SharePositionScheduleEntity schedule =
                scheduleRepo.findByShareId(bean.getId())
                            .orElse(new SharePositionScheduleEntity());

            schedule.setShareId(bean.getId());
            schedule.setData(bean.getSharePositionScheduleBean().getData());
            schedule.setAdminId(user.getAdminId());
            schedule.setUserId(user.getId());

            scheduleRepo.save(schedule);
        }
    }

    public void deleteShare(Long id) {
        scheduleRepo.deleteByShareId(id);
        apiRepo.deleteById(id);
    }
    
    
    
    
    public Page<SharePositionBean> getAllShares(Users user, Pageable pageable, String search) {

        String searchParam = search != null ? search.trim() : "";

        Page<ApiManagerEntity> sharesPage = apiRepo.findByUserIdWithSearch(user.getId(), searchParam, pageable);

        // 🔹 Formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 🔹 Fetch all deviceIds first
        List<Long> allDeviceIds = sharesPage.getContent().stream()
                .flatMap(share -> Arrays.stream(
                        Optional.ofNullable(share.getDeviceId()).orElse("").split(",")
                ))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .distinct()
                .toList();

        // 🔥 FIXED: Safe map (no null key/value crash)
        Map<Long, String> deviceTimezoneMap = deviceRepo.findDeviceTimeZoneByIds(allDeviceIds)
                .stream()
                .filter(row -> row != null && row[0] != null)
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> row[1] != null ? (String) row[1] : "", // ✅ avoid null
                        (existing, replacement) -> existing
                ));

        return sharesPage.map(share -> {

            SharePositionBean bean = new SharePositionBean();

            bean.setId(share.getId());
            bean.setName(share.getName());
            bean.setStatus(share.isStatus());
            bean.setDeviceId(share.getDeviceId());
            bean.setValidTime(share.getValidTime());
            bean.setEmail(share.getEmail());
            bean.setPhone(share.getPhone());
            bean.setBaseUrl(share.getBaseUrl());
            bean.setAccessCode(share.getAccessCode());
            bean.setDeleteAfterExpiration(share.isDeleteAfterExpiration());

            // Convert "7,151" → List<Long>
            List<Long> deviceIds = Optional.ofNullable(share.getDeviceId()).stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList();

            List<String> names = deviceRepo.findDeviceNamesByIds(deviceIds);
            bean.setDeviceName(String.join(",", names));

            // 🔥 FIXED: ignore null + empty timezone
            String deviceTimezone = deviceIds.stream()
                    .map(deviceTimezoneMap::get)
                    .filter(tz -> tz != null && !tz.isEmpty())
                    .findFirst()
                    .orElse(null);

            // ✅ Convert String → LocalDateTime
            LocalDateTime startTime = (share.getAccessStartTime() != null && !share.getAccessStartTime().isEmpty())
                    ? LocalDateTime.parse(share.getAccessStartTime(), formatter)
                    : null;

            LocalDateTime endTime = (share.getAccessEndTime() != null && !share.getAccessEndTime().isEmpty())
                    ? LocalDateTime.parse(share.getAccessEndTime(), formatter)
                    : null;

            // ✅ Apply helper + convert back to String
            bean.setAccessStartTime(
                    startTime != null
                            ? DateTimeHelper.convertToUserZone(
                                    startTime,
                                    deviceTimezone,
                                    user.getTimezone()
                              ).format(formatter)
                            : null
            );

            bean.setAccessEndTime(
                    endTime != null
                            ? DateTimeHelper.convertToUserZone(
                                    endTime,
                                    deviceTimezone,
                                    user.getTimezone()
                              ).format(formatter)
                            : null
            );

            return bean;
        });
    }


    public SharePositionBean getShareById(Long id, Users user) {

        // Fetch the share entity
        ApiManagerEntity share = apiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        // Security check
        if (!share.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Create the SharePositionBean
        SharePositionBean bean = new SharePositionBean();
        bean.setId(share.getId());
        bean.setName(share.getName());
        bean.setStatus(share.isStatus());
        bean.setDeviceId(share.getDeviceId());
        bean.setValidTime(share.getValidTime());
        bean.setEmail(share.getEmail());
        bean.setPhone(share.getPhone());
        bean.setBaseUrl(share.getBaseUrl());
        bean.setAccessCode(share.getAccessCode());
        bean.setAccessStartTime(share.getAccessStartTime());
        bean.setAccessEndTime(share.getAccessEndTime());
        
        
        bean.setDeleteAfterExpiration(share.isDeleteAfterExpiration());
        bean.setUserId(share.getUserId());
        bean.setAdminId(share.getAdminId());

        // Convert device IDs to names if needed
        if (share.getDeviceId() != null && !share.getDeviceId().isEmpty()) {
            List<Long> deviceIds = Arrays.stream(share.getDeviceId().split(","))
                    .map(Long::valueOf)
                    .toList();
            List<String> names = deviceRepo.findDeviceNamesByIds(deviceIds);
            bean.setDeviceName(String.join(",", names));
        }

        // Fetch schedule entity and convert to SharePositionSheduleBean
        SharePositionScheduleEntity scheduleEntity =
                scheduleRepo.findByShareId(id).orElse(null);

        if (scheduleEntity != null) {
            SharePositionSheduleBean scheduleBean = new SharePositionSheduleBean();
            scheduleBean.setId(scheduleEntity.getId());
            scheduleBean.setSharePositionId(scheduleEntity.getShareId()); 
            scheduleBean.setData(scheduleEntity.getData());
            bean.setSharePositionScheduleBean(scheduleBean);
        }

        return bean;
    }


    public APISuccessReponse getWebLiveData(String uniqueCode) throws GenericException {

        Long accessCode;
        try {
            accessCode = Long.valueOf(uniqueCode);
        } catch (NumberFormatException e) {
            throw new GenericException(
                    HttpStatus.BAD_REQUEST, "Invalid access code");
        }

        long currentTime = System.currentTimeMillis();

        List<SharePositionBean> shares =
                apiRepo.findActiveShares(accessCode);

        if (shares.isEmpty()) {
            throw new GenericException(
                    HttpStatus.BAD_REQUEST,
                    "API has expired! Please contact the service provider.");
        }

        boolean valid = shares.stream().anyMatch(bean -> {

            // ================= CUSTOM SCHEDULE =================
            if ("custom".equalsIgnoreCase(bean.getValidTime())) {

                SharePositionScheduleEntity scheduleEntity =
                        scheduleRepo.findByShareId(bean.getId()).orElse(null);

                if (scheduleEntity == null || scheduleEntity.getData() == null) {
                    return false;
                }

                ZonedDateTime now =
                        ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

                // 0 = Sunday (matches your DB)
                int currentDay = now.getDayOfWeek().getValue() % 7;
                int currentHour = now.getHour();
                return scheduleEntity.getData().stream()
                        .filter(slot -> slot.isSelected())
                        .anyMatch(slot ->
                                Integer.parseInt(slot.getDay()) == currentDay
                                && Integer.parseInt(slot.getTime()) == currentHour
                        );
            }

            // ================= NON-CUSTOM TIME WINDOW =================
            if (bean.getAccessStartTime() == null
                    || bean.getAccessEndTime() == null) {
                return false;
            }

            long start = TimeZoneConverter
                    .convertAsiaKolkataToUTCshare(bean.getAccessStartTime());

            long end = TimeZoneConverter
                    .convertAsiaKolkataToUTCshare(bean.getAccessEndTime());

            return currentTime >= start && currentTime <= end;
        });

        if (!valid) {
            throw new GenericException(
                    HttpStatus.BAD_REQUEST,
                    "API Service is not active for the current time window.");
        }

        // ================= FETCH LIVE DATA =================
        List<Object[]> liveData =
                liveDataRepository.findExternalLiveDataRaw(accessCode);

        if (liveData.isEmpty()) {
            throw new GenericException(
                    HttpStatus.BAD_REQUEST,
                    "No live data found for this API.");
        }

        List<ExternalLiveDataDTO> dtos = liveData.stream()
                .map(r -> new ExternalLiveDataDTO(
                        ((Number) r[0]).doubleValue(),
                        ((Number) r[1]).doubleValue(),
                        (String) r[2],
                        (String) r[3],
                        ((Number) r[4]).doubleValue(),
                        (String) r[5],
                        Instant.ofEpochSecond(((Number) r[6]).longValue())
                                .atZone(ZoneId.of("Asia/Kolkata"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        (String) r[7]
                ))
                .collect(Collectors.toList());

        return new APISuccessReponse("success", "", dtos);
    }
    
    
    
    
    
    
}
