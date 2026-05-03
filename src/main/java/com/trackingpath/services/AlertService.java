
package com.trackingpath.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.*;
import com.trackingpath.entities.*;
import com.trackingpath.exceptions.IncorrectArgumentException;
import com.trackingpath.exceptions.ResourceNotFoundException;
import com.trackingpath.repositories.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertDeviceMappingRepository deviceMappingRepository;
    private final AlertDeviceCommandMappingRepository alertDeviceCommandMappingRepository;
    private final AlertDetailsRepository alertDetailsRepository;
    private final AlertPoiMappingRepository alertPoiMappingRepository;
    private final AlertGeofenceMappingRepository alertGeofenceMappingRepository;
    private final AlertRouteMappingRepository alertRouteMappingRepository;
    private final AlertScheduleRepository alertScheduleRepository;
    private final AlertNotificationRepository alertNotificationRepository;
    private final AlertUserMappingRepository alertUserMappingRepository;
    
    private final  CommandRepository commandRepository;
    //helper methods
    
    private boolean hasAtLeastOneSelectedSlot(List<SlotDTO> slots) {

        if (slots == null || slots.isEmpty()) {
            return false;
        }

        for (SlotDTO slot : slots) {
            if (Boolean.TRUE.equals(slot.getSelected())) {
                return true;
            }
        }

        return false;
    }

    // ======================================================
    // CREATE ALERT
    // ======================================================
    public Long createAlert(AlertSettingDTO dto, Users user) {

        Alert alert = alertRepository.save(
                Alert.builder()
                        .alertName(dto.getAlertName())
                        .userId(user.getId())
                        .adminId(user.getAdminId())
                        .status("ACTIVE")
                        .build()
        );

        // 1️⃣ DEVICE MAPPING
        if (dto.getAlertDeviceMappingDTO() != null
                && dto.getAlertDeviceMappingDTO().getDeviceIds() != null) {

            for (Long devId : dto.getAlertDeviceMappingDTO().getDeviceIds()) {
                deviceMappingRepository.save(
                        AlertDeviceMapping.builder()
                                .alert(alert)
                                .deviceId(devId)
                                .userId(user.getId())
                                .adminId(user.getAdminId())
                                .build()
                );
            }

            // 2️⃣ DEVICE COMMAND
            if (dto.getAlertDeviceCommandDTO() != null &&
            	    dto.getAlertDeviceCommandDTO().getCommandName() != null &&
            	    !dto.getAlertDeviceCommandDTO().getCommandName().isBlank()) {
                alertDeviceCommandMappingRepository.save(
                        AlertDeviceCommandMapping.builder()
                                .alert(alert)
                                .commandName(dto.getAlertDeviceCommandDTO().getCommandName())
                                .userId(user.getId())
                                .adminId(user.getAdminId())
                                .build()
                );
            }
        }

        // 3️⃣ ALERT DETAILS + POI
     // 3️⃣ ALERT DETAILS + POI
        if (dto.getAlertDetailsDTO() != null &&
            dto.getAlertDetailsDTO().getAlertType() != null &&
            !dto.getAlertDetailsDTO().getAlertType().isBlank()) {

            AlertDetailsDTO d = dto.getAlertDetailsDTO();

            boolean isStop = "STOPPAGE_DURATION".equals(d.getAlertType());
            boolean isIdle = "IDLE_DURATION".equals(d.getAlertType());
            boolean isPoiStop = "POI_STOP_DURATION".equals(d.getAlertType());
            boolean isPoiIdle = "POI_IDLE_DURATION".equals(d.getAlertType());

            if (isStop && (d.getStopDuration() == null || d.getStopDuration() <= 0))
                throw new IncorrectArgumentException("Stop duration invalid");

            if (isIdle && (d.getIdleDuration() == null || d.getIdleDuration() <= 0))
                throw new IncorrectArgumentException("Idle duration invalid");

            Integer poiStop = null;
            Integer poiIdle = null;

            if (isPoiStop) {
                if (d.getPoiStopDuration() == null || d.getPoiStopDuration() <= 0)
                    throw new IncorrectArgumentException("POI stop duration invalid");
                poiStop = d.getPoiStopDuration();
            }

            if (isPoiIdle) {
                if (d.getPoiIdleDuration() == null || d.getPoiIdleDuration() <= 0)
                    throw new IncorrectArgumentException("POI idle duration invalid");
                poiIdle = d.getPoiIdleDuration();
            }

            String alertTypeValue = d.getAlertType();
            String adas_dms_cat = null;

            List<String> events = new ArrayList<>();
            List<String> categories = new ArrayList<>();

            // ADAS
            if (d.getAdasEvents() != null && !d.getAdasEvents().isEmpty()) {
                events.addAll(d.getAdasEvents());
                categories.add("ADAS");
            }

            // DMS
            if (d.getDmsEvents() != null && !d.getDmsEvents().isEmpty()) {
                events.addAll(d.getDmsEvents());
                categories.add("DMS");
            }

            // If ADAS/DMS events exist, override alertTypeValue
            if (!events.isEmpty()) {
                alertTypeValue = String.join(",", events);
            }

            // Category value
            if (!categories.isEmpty()) {
                adas_dms_cat = String.join(",", categories);
            }

            alert.setAlertDetails(
                AlertDetails.builder()
                    .alert(alert)
                    .alertType(alertTypeValue)
                    .overspeed(d.getOverspeed())
                    .stopDuration(d.getStopDuration())
                    .idleDuration(d.getIdleDuration())
                    .ignition(d.getIgnition())
                    .lowSpeed(d.getLowspeed())
                    .sos(d.getSos())
                    .driverChangeIds(d.getDriverChangeIds() == null ? null :
                            String.join(",", d.getDriverChangeIds()))
                    .driverChangeAuth(d.getDriverChangeAuth())
                    .poiStopDuration(poiStop)
                    .poiIdleDuration(poiIdle)
                    .vibration(d.getVibration())
                    .movement(d.getMovement())
                    .falldown(d.getFalldown())
                    .lowpower(d.getLowpower())
                    .lowbattery(d.getLowbattery())
                    .powercut(d.getPowercut())
                    .powerrestored(d.getPowerrestored())
                    .userId(user.getId())
                    .adminId(user.getAdminId())
                    .adasDmsCategory(adas_dms_cat)
                    .build()
            );

            // -------- POI MAPPING --------
            if (isPoiStop || isPoiIdle) {

                AlertPoiMapping.AlertPoiMappingBuilder builder =
                        AlertPoiMapping.builder()
                                .alert(alert)
                                .poiId(
                                    d.getPoiIds() == null || d.getPoiIds().isEmpty()
                                        ? null
                                        : String.join(",", d.getPoiIds())
                                )
                                .idleStopPoi(d.getAlertType())
                                .userId(user.getId())
                                .adminId(user.getAdminId());

                if (isPoiStop) {
                    builder.stopDuration(poiStop)
                           .idleDuration(null);
                } else {
                    builder.idleDuration(poiIdle)
                           .stopDuration(null);
                }

                alertPoiMappingRepository.save(builder.build());
            }
        }

        // 5️⃣ GEOFENCE MAPPING
        if (dto.getAlertGeofenceMappingDTO() != null
                && dto.getAlertGeofenceMappingDTO().getGeofenceIds() != null) {

            for (Long geofenceId : dto.getAlertGeofenceMappingDTO().getGeofenceIds()) {
                alertGeofenceMappingRepository.save(
                        AlertGeofenceMapping.builder()
                                .alert(alert)
                                .geofenceId(geofenceId)
                                .geofenceInOut(dto.getAlertGeofenceMappingDTO().getGeofenceInOut())
                                .userId(user.getId())
                                .adminId(user.getAdminId())
                                .build()
                );
            }
        }

        // 6️⃣ ROUTE MAPPING
        if (dto.getAlertRouteMappingDTO() != null
                && dto.getAlertRouteMappingDTO().getRouteIds() != null) {

            for (Long routeId : dto.getAlertRouteMappingDTO().getRouteIds()) {
                alertRouteMappingRepository.save(
                        AlertRouteMapping.builder()
                                .alert(alert)
                                .routeId(routeId)
                                .routeInOut(dto.getAlertRouteMappingDTO().getRouteInOut())
                                .userId(user.getId())
                                .adminId(user.getAdminId())
                                .build()
                );
            }
        }

       
        
        // 7️⃣ SCHEDULE
        if (dto.getAlertScheduleDTO() != null &&
        	    hasAtLeastOneSelectedSlot(dto.getAlertScheduleDTO().getData())) {
            alertScheduleRepository.save(
                    AlertSchedule.builder()
                            .alert(alert)
                            .status(dto.getAlertScheduleDTO().getStatus())
                            .data(dto.getAlertScheduleDTO().getData())
                            .userId(user.getId())
                            .adminId(user.getAdminId())
                            .build()
            );
        }

        // 8️⃣ NOTIFICATION
        AlertNotificationDTO n = dto.getAlertNotificationDTO();

        if (n != null &&
            (n.getIgnoreNotification() != null ||
             n.getSoundNotification() != null ||
             n.getPopupNotification() != null ||
             Boolean.TRUE.equals(n.getAppPushNotification()) ||
             n.getEmailNotification() != null ||
             n.getWebhookNotification() != null ||
             n.getNotificationColor() != null)) {
            
            alertNotificationRepository.save(
                    AlertNotification.builder()
                            .alert(alert)
                            .ignoreNotification(n.getIgnoreNotification())
                            .soundNotification(n.getSoundNotification())
                            .popupNotification(n.getPopupNotification())
                            .appPushNotification(n.getAppPushNotification())
                            .emailNotification(n.getEmailNotification())
                            .webhookNotification(n.getWebhookNotification())
                            .notificationColor(n.getNotificationColor())
                            .userId(user.getId())
                            .adminId(user.getAdminId())
                            .build()
            );
        }

        // 9️⃣ USERS
        if (dto.getAlertUserDTO() != null &&
        	    dto.getAlertUserDTO().getUser_ids() != null) {

        	    for (Long uid : dto.getAlertUserDTO().getUser_ids()) {

        	        alert.addAlertUser(
        	            AlertUserMapping.builder()
        	                .user_id(uid)                  // JSON user
        	                .admin_id(user.getAdminId())   // logged admin
        	                .build()
        	        );
        	    }
        	}

        alertRepository.save(alert);
        return alert.getId();
    }

    // ======================================================
    // GET ALERTS DATA
    // ======================================================
    public Page<AlertDataDTO> getAlertsData(Users user, String search, Pageable pageable) {
    	
    	String searchParam = search != null ? search.trim() : "";

		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

		if (isAdmin) {
			return alertRepository.findAlertsDataForAdmin(user.getAdminId(), searchParam, pageable);
		} else {
			return alertRepository.findAlertsDataForUser(user.getAdminId(), user.getId(), searchParam, pageable);
		}
	}



    // ======================================================
    // GET ALERT BY ID
    // ======================================================
    public AlertSettingDTO getAlertById(Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        AlertSettingDTO dto = new AlertSettingDTO();
        dto.setId(alert.getId());
        dto.setAlertName(alert.getAlertName());

        // =========================
        // ALERT DETAILS
        // =========================
        List<AlertDetails> detailsList = alertDetailsRepository.findByAlert(alert);
        if (!detailsList.isEmpty()) {
            AlertDetails details = detailsList.get(0);
            AlertDetailsDTO detailsDTO = AlertDetailsDTO.builder()
                    .id(details.getId())
                    .alertId(alert.getId())
                    .alertType(details.getAlertType())
                    .adasDmsCategory(details.getAdasDmsCategory())
                    .overspeed(details.getOverspeed())
                    .stopDuration(details.getStopDuration())
                    .idleDuration(details.getIdleDuration())
                    .ignition(details.getIgnition())
                    .sos(details.getSos())
                    .lowspeed(details.getLowSpeed())
                    .driverChangeAuth(details.getDriverChangeAuth())
                    .poiStopDuration(details.getPoiStopDuration())
                    .poiIdleDuration(details.getPoiIdleDuration())
                    .vibration(details.getVibration())
                    .movement(details.getMovement())
                    .falldown(details.getFalldown())
                    .lowpower(details.getLowpower())
                    .lowbattery(details.getLowbattery())
                    .powercut(details.getPowercut())
                    .powerrestored(details.getPowerrestored())
                    .build();
            //System.out.println("poi stop duration: "+details.getPoiStopDuration()+"poi idle duration: "+details.getPoiIdleDuration());
            // driver IDs
            if (details.getDriverChangeIds() != null && !details.getDriverChangeIds().isBlank()) {
                detailsDTO.setDriverChangeIds(
                    List.of(details.getDriverChangeIds().split(",")).stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList()
                );
            }

            // POI IDs: collect all mappings
            List<AlertPoiMapping> poiMappings = alertPoiMappingRepository.findByAlert(alert);
            List<String> poiIds = poiMappings.stream()
                    .flatMap(p -> p.getPoiId() != null
                            ? List.of(p.getPoiId().split(",")).stream()
                            : List.<String>of().stream())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            detailsDTO.setPoiIds(poiIds);

            dto.setAlertDetailsDTO(detailsDTO);
        }

        // =========================
        // DEVICE MAPPING
        // =========================
        List<AlertDeviceMapping> deviceMappings = deviceMappingRepository.findByAlert(alert);
        if (!deviceMappings.isEmpty()) {
            dto.setAlertDeviceMappingDTO(AlertDeviceMappingDTO.builder()
                    .id(deviceMappings.get(0).getId()) // if multiple, take first
                    .alertId(alert.getId())
                    .deviceIds(deviceMappings.stream()
                            .map(AlertDeviceMapping::getDeviceId)
                            .toList())
                    .build());
        }

        // =========================
        // DEVICE COMMAND
        // =========================
        List<AlertDeviceCommandMapping> commands = alertDeviceCommandMappingRepository.findByAlert(alert);
        if (!commands.isEmpty()) {
            AlertDeviceCommandMapping cmd = commands.get(0); // usually 1
            dto.setAlertDeviceCommandDTO(AlertDeviceCommandDTO.builder()
                    .alertId(alert.getId())
                    .commandName(cmd.getCommandName())
                    .build());
        }

        // =========================
        // NOTIFICATIONS
        // =========================
        List<AlertNotification> notifications = alertNotificationRepository.findByAlert(alert);
        if (!notifications.isEmpty()) {
            AlertNotification n = notifications.get(0);
            dto.setAlertNotificationDTO(AlertNotificationDTO.builder()
                    .id(n.getId())
                    .alertId(alert.getId())
                    .ignoreNotification(n.getIgnoreNotification())
                    .soundNotification(n.getSoundNotification())
                    .popupNotification(n.getPopupNotification())
                    .appPushNotification(n.getAppPushNotification())
                    .emailNotification(n.getEmailNotification())
                    .webhookNotification(n.getWebhookNotification())
                    .notificationColor(n.getNotificationColor())
                    .build());
        }

        // =========================
        // SCHEDULE
        // =========================
        List<AlertSchedule> schedules = alertScheduleRepository.findByAlert(alert);
        if (!schedules.isEmpty()) {
            AlertSchedule s = schedules.get(0);
            dto.setAlertScheduleDTO(AlertScheduleDTO.builder()
                    .id(s.getId())
                    .alertId(alert.getId())
                    .status(s.getStatus())
                    .data(s.getData())
                    .build());
        }

        // =========================
        // GEOFENCE
        // =========================
        List<AlertGeofenceMapping> geofences =
                alertGeofenceMappingRepository.findByAlert(alert);

        if (!geofences.isEmpty()) {

            // collect all geofence IDs
            List<Long> geofenceIds = geofences.stream()
                    .map(AlertGeofenceMapping::getGeofenceId)
                    .collect(Collectors.toList());

            AlertGeofenceMapping first = geofences.get(0); // for in/out flag

            dto.setAlertGeofenceMappingDTO(
                    AlertGeofenceMappingDTO.builder()
                            .id(first.getId())
                            .alertId(alert.getId())
                            .geofenceIds(geofenceIds)
                            .geofenceInOut(first.getGeofenceInOut())
                            .build()
            );
        }

        // =========================
        // ROUTE MAPPING
        // =========================
        List<AlertRouteMapping> routes =
                alertRouteMappingRepository.findByAlert(alert);

        if (!routes.isEmpty()) {

            // collect all route IDs
            List<Long> routeIds = routes.stream()
                    .map(AlertRouteMapping::getRouteId)
                    .collect(Collectors.toList());

            AlertRouteMapping first = routes.get(0); // for in/out flag

            dto.setAlertRouteMappingDTO(
                    AlertRouteMappingDTO.builder()
                            .id(first.getId())
                            .alertId(alert.getId())
                            .routeIds(routeIds)
                            .routeInOut(first.getRouteInOut())
                            .build()
            );
        }


        // =========================
        // USERS
        // =========================
        List<AlertUserMapping> alertUsers = alertUserMappingRepository.findByAlert(alert);
        if (!alertUsers.isEmpty()) {
            List<Long> fetchedUserIds = alertUsers.stream()
                    .map(AlertUserMapping::getUser_id)
                    .toList();
                    dto.setAlertUserDTO(AlertUserDTO.builder()
                    .id(alertUsers.get(0).getId())
                    .alertId(alert.getId())
                    .adminId(user.getAdminId())
                    .user_ids(fetchedUserIds)
                    .build());
        }

        return dto;
    }



    // ======================================================
    // DELETE ALERT
    // ======================================================
    @Transactional
    public boolean deleteAlert(Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getAdminId().equals(user.getAdminId())) {
            return false;
        }

        alertRepository.delete(alert);

        return true;
    }

    // ======================================================
    // UPDATE ALERT
    // ======================================================
	/*
	 * @Transactional(dontRollbackOn = IncorrectArgumentException.class) public
	 * boolean updateAlert(AlertSettingDTO dto, Users user) {
	 * 
	 * if (dto.getId() == null || dto.getId() <= 0) { throw new
	 * IllegalArgumentException("Valid alert id is required"); }
	 * 
	 * Alert alert = alertRepository.findById(dto.getId()) .orElseThrow(() -> new
	 * ResourceNotFoundException("Alert not found"));
	 * 
	 * if (!alert.getAdminId().equals(user.getAdminId())) { return false; //
	 * unauthorized }
	 * 
	 * // ========================= // Parent fields // ========================= if
	 * (dto.getAlertName() != null) { alert.setAlertName(dto.getAlertName()); }
	 * 
	 * // ========================= // Device mappings // =========================
	 * if (dto.getAlertDeviceMappingDTO() != null &&
	 * dto.getAlertDeviceMappingDTO().getDeviceIds() != null) {
	 * 
	 * if (dto.getAlertDeviceMappingDTO() != null &&
	 * dto.getAlertDeviceMappingDTO().getDeviceIds() != null) {
	 * 
	 * // REMOVE old mappings (safe orphan removal)
	 * alert.getDeviceMappings().clear();
	 * 
	 * // ADD new mappings for (Long deviceId :
	 * dto.getAlertDeviceMappingDTO().getDeviceIds()) { if (deviceId != null &&
	 * deviceId > 0) { alert.addDeviceMapping( AlertDeviceMapping.builder()
	 * .deviceId(deviceId) .userId(user.getId()) .adminId(user.getAdminId())
	 * .build() ); } } } }
	 * 
	 * // ========================= // Device commands // =========================
	 * if (dto.getAlertDeviceCommandDTO() != null &&
	 * dto.getAlertDeviceCommandDTO().getCommandName() != null &&
	 * !dto.getAlertDeviceCommandDTO().getCommandName().isBlank()) {
	 * 
	 * // Remove old mapping (orphanRemoval will delete it)
	 * alert.setDeviceCommand(null);
	 * 
	 * // Create & set new mapping AlertDeviceCommandMapping cmd =
	 * AlertDeviceCommandMapping.builder()
	 * .commandName(dto.getAlertDeviceCommandDTO().getCommandName())
	 * .userId(user.getId()) .adminId(user.getAdminId()) .build();
	 * 
	 * alert.setDeviceCommand(cmd); }
	 * 
	 * // ========================= // Geofence mappings //
	 * ========================= if (dto.getAlertGeofenceMappingDTO() != null &&
	 * dto.getAlertGeofenceMappingDTO().getGeofenceIds() != null) {
	 * 
	 * alert.getGeofenceMappings().clear(); for (Long geoId :
	 * dto.getAlertGeofenceMappingDTO().getGeofenceIds()) {
	 * alert.addGeofenceMapping(AlertGeofenceMapping.builder() .alert(alert)
	 * .geofenceId(geoId)
	 * .geofenceInOut(dto.getAlertGeofenceMappingDTO().getGeofenceInOut())
	 * .userId(user.getId()) .adminId(user.getAdminId()) .build()); } }
	 * 
	 * // ========================= // Route mappings // =========================
	 * if (dto.getAlertRouteMappingDTO() != null &&
	 * dto.getAlertRouteMappingDTO().getRouteIds() != null) {
	 * 
	 * alert.getRouteMappings().clear(); for (Long routeId :
	 * dto.getAlertRouteMappingDTO().getRouteIds()) {
	 * alert.addRouteMapping(AlertRouteMapping.builder() .alert(alert)
	 * .routeId(routeId) .routeInOut(dto.getAlertRouteMappingDTO().getRouteInOut())
	 * .userId(user.getId()) .adminId(user.getAdminId()) .build()); } }
	 * 
	 * // ========================= // Alert details + POI //
	 * =========================
	 * 
	 * if (dto.getAlertDetailsDTO() != null &&
	 * dto.getAlertDetailsDTO().getAlertType() != null &&
	 * !dto.getAlertDetailsDTO().getAlertType().isBlank()) {
	 * 
	 * AlertDetailsDTO detailsDTO = dto.getAlertDetailsDTO(); String alertType =
	 * detailsDTO.getAlertType();
	 * 
	 * boolean isStop = "STOPPAGE_DURATION".equals(alertType); boolean isIdle =
	 * "IDLE_DURATION".equals(alertType); boolean isPoiStop =
	 * "POI_STOP_DURATION".equals(alertType); boolean isPoiIdle =
	 * "POI_IDLE_DURATION".equals(alertType);
	 * 
	 * // ========================= // VALIDATIONS // =========================
	 * 
	 * if (isStop && (detailsDTO.getStopDuration() == null ||
	 * detailsDTO.getStopDuration() <= 0)) { throw new
	 * IncorrectArgumentException("Stop Duration must be greater than 0"); }
	 * 
	 * if (isIdle && (detailsDTO.getIdleDuration() == null ||
	 * detailsDTO.getIdleDuration() <= 0)) { throw new
	 * IncorrectArgumentException("Idle Duration must be greater than 0"); }
	 * 
	 * if (isPoiStop && (detailsDTO.getPoiStopDuration() == null ||
	 * detailsDTO.getPoiStopDuration() <= 0)) { throw new
	 * IncorrectArgumentException("POI Stop Duration must be greater than 0"); }
	 * 
	 * if (isPoiIdle && (detailsDTO.getPoiIdleDuration() == null ||
	 * detailsDTO.getPoiIdleDuration() <= 0)) { throw new
	 * IncorrectArgumentException("POI Idle Duration must be greater than 0"); }
	 * 
	 * // ========================= // REMOVE OLD DETAILS (important) //
	 * ========================= alert.setAlertDetails(null);
	 * 
	 * // ========================= // CREATE NEW DETAILS //
	 * ========================= AlertDetails details = AlertDetails.builder()
	 * .alert(alert) .alertType(alertType) .overspeed(detailsDTO.getOverspeed())
	 * .stopDuration(isStop ? detailsDTO.getStopDuration() : null)
	 * .idleDuration(isIdle ? detailsDTO.getIdleDuration() : null)
	 * .poiStopDuration(isPoiStop ? detailsDTO.getPoiStopDuration() : null)
	 * .poiIdleDuration(isPoiIdle ? detailsDTO.getPoiIdleDuration() : null)
	 * .ignition(detailsDTO.getIgnition()) .lowSpeed(detailsDTO.getLowspeed())
	 * .sos(detailsDTO.getSos()) .driverChangeIds(detailsDTO.getDriverChangeIds() !=
	 * null ? String.join(",", detailsDTO.getDriverChangeIds()) : null)
	 * .driverChangeAuth(detailsDTO.getDriverChangeAuth())
	 * .vibration(detailsDTO.getVibration()) .movement(detailsDTO.getMovement())
	 * .falldown(detailsDTO.getFalldown()) .lowpower(detailsDTO.getLowpower())
	 * .lowbattery(detailsDTO.getLowbattery()) .powercut(detailsDTO.getPowercut())
	 * .powerrestored(detailsDTO.getPowerrestored()) .userId(user.getId())
	 * .adminId(user.getAdminId()) .build();
	 * 
	 * alert.setAlertDetails(details);
	 * 
	 * // ========================= // POI MAPPINGS // ========================= if
	 * (detailsDTO.getPoiIds() != null && !detailsDTO.getPoiIds().isEmpty()) {
	 * 
	 * alert.getPoiMappings().clear();
	 * 
	 * AlertPoiMapping.AlertPoiMappingBuilder builder = AlertPoiMapping.builder()
	 * .alert(alert) .poiId(String.join(",", detailsDTO.getPoiIds()))
	 * .idleStopPoi(alertType) .userId(user.getId()) .adminId(user.getAdminId());
	 * 
	 * if (isPoiIdle) { builder.idleDuration(detailsDTO.getPoiIdleDuration())
	 * .stopDuration(null); } else if (isPoiStop) {
	 * builder.stopDuration(detailsDTO.getPoiStopDuration()) .idleDuration(null); }
	 * 
	 * alert.addPoiMapping(builder.build()); } }
	 * 
	 * // ========================= // Schedule // ========================= if
	 * (dto.getAlertScheduleDTO() != null &&
	 * hasAtLeastOneSelectedSlot(dto.getAlertScheduleDTO().getData())) {
	 * alert.getSchedules().clear(); alert.addSchedule(AlertSchedule.builder()
	 * .alert(alert) .status(dto.getAlertScheduleDTO().getStatus())
	 * .data(dto.getAlertScheduleDTO().getData()) .userId(user.getId())
	 * .adminId(user.getAdminId()) .build()); }
	 * 
	 * // ========================= // Notifications // =========================
	 * AlertNotificationDTO n = dto.getAlertNotificationDTO();
	 * 
	 * if (n != null && (n.getIgnoreNotification() != null ||
	 * n.getSoundNotification() != null || n.getPopupNotification() != null ||
	 * Boolean.TRUE.equals(n.getAppPushNotification()) || n.getEmailNotification()
	 * != null || n.getWebhookNotification() != null || n.getNotificationColor() !=
	 * null)) {
	 * 
	 * alert.getNotifications().clear();
	 * 
	 * alert.addNotification(AlertNotification.builder() .alert(alert)
	 * .ignoreNotification(n.getIgnoreNotification())
	 * .soundNotification(n.getSoundNotification())
	 * .popupNotification(n.getPopupNotification())
	 * .appPushNotification(n.getAppPushNotification())
	 * .emailNotification(n.getEmailNotification())
	 * .webhookNotification(n.getWebhookNotification())
	 * .notificationColor(n.getNotificationColor()) .userId(user.getId())
	 * .adminId(user.getAdminId()) .build()); }
	 * 
	 * // ========================= // Alert users // =========================
	 * alert.getAlertUsers().clear();
	 * 
	 * if (dto.getAlertUserDTO() != null && dto.getAlertUserDTO().getUser_ids() !=
	 * null) {
	 * 
	 * for (Long uid : dto.getAlertUserDTO().getUser_ids()) {
	 * 
	 * alert.addAlertUser( AlertUserMapping.builder() .user_id(uid)
	 * .admin_id(user.getAdminId()) .build() ); } }
	 * 
	 * 
	 * 
	 * alertRepository.save(alert); return true; }
	 */
    @Transactional
    public boolean updateAlertData(AlertSettingDTO dto, Users user) {

            if (dto.getId() == null || dto.getId() <= 0) {
                    throw new IllegalArgumentException("Valid alert id required");
            }

            int updated = alertRepository.forceUpdateAlertName(
                            dto.getId(),
                            dto.getAlertName(),
                            user.getAdminId()
            );

            return updated > 0;
    }
    @Transactional
    public void updateAlertDeviceMapping(AlertSettingDTO dto, Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        // Delete old mappings
        deviceMappingRepository.deleteByAlertId(alertId);

        // Insert new
        for (Long deviceId : dto.getAlertDeviceMappingDTO().getDeviceIds()) {

            AlertDeviceMapping mapping = AlertDeviceMapping.builder()
                    .alert(alert)
                    .deviceId(deviceId)
                    .userId(user.getId())
                    .adminId(user.getAdminId())
                    .build();

            deviceMappingRepository.save(mapping);
        }
    }
    @Transactional
    public void updateAlertDeviceCommand(AlertSettingDTO dto, Long alertId, Users user) {

        if (dto.getAlertDeviceCommandDTO() == null ||
            dto.getAlertDeviceCommandDTO().getCommandName() == null ||
            dto.getAlertDeviceCommandDTO().getCommandName().isBlank()) {
            return;
        }

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        // Delete old command
        alertDeviceCommandMappingRepository.deleteByAlertId(alertId);

        // Insert new command
        AlertDeviceCommandMapping cmd = AlertDeviceCommandMapping.builder()
                .alert(alert)
                .commandName(dto.getAlertDeviceCommandDTO().getCommandName())
                .userId(user.getId())
                .adminId(user.getAdminId())
                .build();

        alertDeviceCommandMappingRepository.save(cmd);
    }
    @Transactional
    public void updateAlertDetails(AlertSettingDTO dto, Long alertId, Users user) {

        if (dto.getAlertDetailsDTO() == null) return;

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertDetailsRepository.deleteByAlertId(alertId);

        AlertDetailsDTO d = dto.getAlertDetailsDTO();
        
        
        
        String alertTypeValue = d.getAlertType();
        String adas_dms_cat = null;

        List<String> events = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        // ADAS
        if (d.getAdasEvents() != null && !d.getAdasEvents().isEmpty()) {
            events.addAll(d.getAdasEvents());
            categories.add("ADAS");
        }

        // DMS
        if (d.getDmsEvents() != null && !d.getDmsEvents().isEmpty()) {
            events.addAll(d.getDmsEvents());
            categories.add("DMS");
        }

        // If ADAS/DMS events exist, override alertTypeValue
        if (!events.isEmpty()) {
            alertTypeValue = String.join(",", events);
        }

        // Category value
        if (!categories.isEmpty()) {
            adas_dms_cat = String.join(",", categories);
        }

        
        
        
        
        

        AlertDetails details = AlertDetails.builder()
                .alert(alert)
                .alertType(alertTypeValue)
                .overspeed(d.getOverspeed())
                .stopDuration(d.getStopDuration())
                .idleDuration(d.getIdleDuration())
                .poiStopDuration(d.getPoiStopDuration())
                .poiIdleDuration(d.getPoiIdleDuration())
                .ignition(d.getIgnition())
                .lowSpeed(d.getLowspeed())
                .sos(d.getSos())
                .driverChangeIds(d.getDriverChangeIds() != null
                        ? String.join(",", d.getDriverChangeIds())
                        : null)
                .driverChangeAuth(d.getDriverChangeAuth())
                .vibration(d.getVibration())
                .movement(d.getMovement())
                .falldown(d.getFalldown())
                .lowpower(d.getLowpower())
                .lowbattery(d.getLowbattery())
                .powercut(d.getPowercut())
                .powerrestored(d.getPowerrestored())
                .userId(user.getId())
                .adminId(user.getAdminId())
                .adasDmsCategory(adas_dms_cat)
                .build();

        alertDetailsRepository.save(details);

        // ---- POI Mapping ----
        if (d.getPoiIds() != null && !d.getPoiIds().isEmpty()) {

            alertPoiMappingRepository.deleteByAlertId(alertId);

            AlertPoiMapping poi = AlertPoiMapping.builder()
                    .alert(alert)
                    .poiId(String.join(",", d.getPoiIds()))
                    .idleStopPoi(d.getAlertType())
                    .idleDuration(d.getPoiIdleDuration())
                    .stopDuration(d.getPoiStopDuration())
                    .userId(user.getId())
                    .adminId(user.getAdminId())
                    .build();

            alertPoiMappingRepository.save(poi);
        }
    }
    @Transactional
    public void updateAlertGeofence(AlertSettingDTO dto, Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertGeofenceMappingRepository.deleteByAlertId(alertId);

        for (Long geoId : dto.getAlertGeofenceMappingDTO().getGeofenceIds()) {

            AlertGeofenceMapping mapping = AlertGeofenceMapping.builder()
                    .alert(alert)
                    .geofenceId(geoId)
                    .geofenceInOut(dto.getAlertGeofenceMappingDTO().getGeofenceInOut())
                    .userId(user.getId())
                    .adminId(user.getAdminId())
                    .build();

            alertGeofenceMappingRepository.save(mapping);
        }
    }
    @Transactional
    public void updateAlertRoute(AlertSettingDTO dto, Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertRouteMappingRepository.deleteByAlertId(alertId);

        for (Long routeId : dto.getAlertRouteMappingDTO().getRouteIds()) {

            AlertRouteMapping mapping = AlertRouteMapping.builder()
                    .alert(alert)
                    .routeId(routeId)
                    .routeInOut(dto.getAlertRouteMappingDTO().getRouteInOut())
                    .userId(user.getId())
                    .adminId(user.getAdminId())
                    .build();

            alertRouteMappingRepository.save(mapping);
        }
    }
    @Transactional
    public void updateAlertScheduling(AlertSettingDTO dto, Long alertId, Users user) {

        if (dto.getAlertScheduleDTO() == null) return;

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertScheduleRepository.deleteByAlertId(alertId);

        AlertSchedule schedule = AlertSchedule.builder()
                .alert(alert)
                .status(dto.getAlertScheduleDTO().getStatus())
                .data(dto.getAlertScheduleDTO().getData())
                .userId(user.getId())
                .adminId(user.getAdminId())
                .build();

        alertScheduleRepository.save(schedule);
    }
    @Transactional
    public void updateAlertNotification(AlertSettingDTO dto, Long alertId, Users user) {

        if (dto.getAlertNotificationDTO() == null) return;

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertNotificationRepository.deleteByAlertId(alertId);

        AlertNotificationDTO n = dto.getAlertNotificationDTO();

        AlertNotification notification = AlertNotification.builder()
                .alert(alert)
                .ignoreNotification(n.getIgnoreNotification())
                .soundNotification(n.getSoundNotification())
                .popupNotification(n.getPopupNotification())
                .appPushNotification(n.getAppPushNotification())
                .emailNotification(n.getEmailNotification())
                .webhookNotification(n.getWebhookNotification())
                .notificationColor(n.getNotificationColor())
                .userId(user.getId())
                .adminId(user.getAdminId())
                .build();

        alertNotificationRepository.save(notification);
    }
    @Transactional
    public void updateAlertUsers(AlertSettingDTO dto, Long alertId, Users user) {

        Alert alert = alertRepository.findById(alertId).orElseThrow();

        alertUserMappingRepository.deleteByAlertId(alertId);

        if (dto.getAlertUserDTO() != null &&
            dto.getAlertUserDTO().getUser_ids() != null) {

            for (Long uid : dto.getAlertUserDTO().getUser_ids()) {

                AlertUserMapping mapping = AlertUserMapping.builder()
                        .alert(alert)
                        .user_id(uid)
                        .admin_id(user.getAdminId())
                        .build();

                alertUserMappingRepository.save(mapping);
            }
        }
    }

    @Transactional
    public boolean toggleAlertStatus(Long alertId, Users user) {
        return alertRepository.toggleStatus(alertId, user.getAdminId()) > 0;
    }

    public List<String> getCommandName(String alertType) {
        return commandRepository.getCommandName(alertType);
    }
    
}

