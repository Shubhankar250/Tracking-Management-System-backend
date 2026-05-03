package com.trackingpath.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.AlertDTO;
import com.trackingpath.dtos.DeviceSettingCustomBean;
import com.trackingpath.dtos.DriverView;
import com.trackingpath.dtos.EventsView;
import com.trackingpath.dtos.LiveDataBean;
import com.trackingpath.dtos.LiveDataDto;
import com.trackingpath.dtos.LiveDataView;
import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.Events;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.LiveDataMapper;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.EventsRepository;
import com.trackingpath.repositories.LiveDataRepository;
import com.trackingpath.repositories.MaintenanceRepository;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveDataService {

	private final DeviceGroupMappingService deviceGroupMappingService;

	private final LiveDataRepository liveDataRepo;
	private final DriverRepository driverRepo;
	private final MaintenanceRepository maintenanceRepo;
	private final EventsRepository eventRepo;
	private final LiveDataMapper liveDataMapper;

	public List<LiveDataDto> getLiveData(Users user, String stime, String etime) {

		
		 List<LiveDataView> rows;

		    boolean isAdmin = user.getRoles() != null &&
		            user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()));
		    if (isAdmin) {

		        rows = liveDataRepo.findLiveData(stime, etime);

		    } else {

		    	List<Long> deviceIds = Arrays.stream(
		    	        Optional.ofNullable(user.getAssign_device_ids()).orElse("")
		    	                .split(",")
		    	    )
		    	    .map(String::trim)
		    	    .filter(s -> !s.isEmpty())
		    	    .map(Long::valueOf)
		    	    .toList();
		        if (deviceIds == null || deviceIds.isEmpty()) {
		            deviceIds = List.of(-1L);
		        }
		        rows = liveDataRepo.findLiveDatabyDeviceIds(deviceIds, stime, etime, user.getId());
		    }

		    List<LiveDataDto> list = new ArrayList<>();

		    for (LiveDataView rs : rows) {


			LiveDataDto bean = new LiveDataDto();

			bean.setDevice_id(rs.getDeviceId());
			bean.setDevice_name(rs.getDeviceName());
			bean.setObjectIcon(rs.getObjectIcon());
			bean.setVehicle_status(rs.getVehicleStatus());
			bean.setAttributes(rs.getAttributes());
			bean.setCourse(rs.getCourse());
			bean.setAltitude(rs.getAltitude());
			bean.setLatitude(rs.getLatitude());
			bean.setLongitude(rs.getLongitude());
			bean.setTail(rs.getTailColor());
			bean.setTail_width(rs.getTailLength());
			bean.setAddress(rs.getAddress());
             bean.setMin_moving_speed(rs.getMinMovingSpeed());
			bean.setGroup_id(rs.getGroupId());
			bean.setGroup_name(rs.getGroupName());
            bean.setUniqueid(rs.getUniqueid());
            bean.setSimCardNumber(rs.getSimCardNumber());
            bean.setDeviceModel(rs.getDeviceModel());
            bean.setModalType(rs.getModalType());
            String devicetimezone = rs.getDeviceTimezone();
            bean.setDevicetimezone(devicetimezone);

       	 bean.setDevicetime(
                 DateTimeHelper.convertToUserZone(
                     rs.getDeviceTime(),
                     devicetimezone,
                     user.getTimezone()
                 )
             );
			
       	LocalDateTime deviceTime = rs.getDeviceTime();

       	String status = "Offline";

       	if (deviceTime != null) {

       	    ZoneId zone;

       	    if (devicetimezone == null || devicetimezone.isBlank()) {
       	        zone = ZoneOffset.UTC; // fallback
       	    } else {
       	        zone = ZoneId.of(devicetimezone);
       	    }

       	    // convert device time -> UTC instant
       	    Instant deviceInstant = deviceTime.atZone(zone).toInstant();

       	    long diffSeconds = Duration.between(deviceInstant, Instant.now()).getSeconds();

       	    if (diffSeconds <= 300) {
       	        status = "online";
       	    }
       	}

          	bean.setStatus(status);

			bean.setServertime(DateTimeHelper.utcToZone(rs.getServerTime(), user.getTimezone()));
			long currentTime = DateTimeUtil.getLocalUnixTime(System.currentTimeMillis() / 1000l, user.getTimezone());	
			LocalDateTime lastMovementLdt = rs.getLastMovementTime();
			LocalDateTime lastIdleLdt = rs.getLastIdleTime();

			if (lastMovementLdt != null) {
			    long lastMovementEpoch =
			            lastMovementLdt
			                    .atZone(ZoneId.of("UTC"))
			                    .toEpochSecond();

			    bean.setLastmovementtime(
			            DateTimeUtil.getTimeDiffInDays(
			                    (currentTime - lastMovementEpoch) * 1000L
			            )
			    );
			}

			if (lastIdleLdt != null) {
			    long lastIdleEpoch =
			            lastIdleLdt
			                    .atZone(ZoneId.of("UTC"))
			                    .toEpochSecond();

			    bean.setLastidletime(
			            DateTimeUtil.getTimeDiffInDays(
			                    (currentTime - lastIdleEpoch) * 1000L
			            )
			    );
			}

			// JSON parsing (same as RowMapper)
			List<DriverView> drivers = driverRepo.findByDevice_Id(rs.getDeviceId());
			bean.setDrivers(drivers);

			bean.setServices(maintenanceRepo.findTop1ByDevice_IdOrderByIdDesc(rs.getDeviceId()));
			bean.setSpeed(rs.getSpeed());
			 DeviceSettingCustomBean deviceBean = new DeviceSettingCustomBean();
			    deviceBean.setIcon_type(rs.getIconType());
			    deviceBean.setMoving_icon_color(rs.getMovingIconColor());
			    deviceBean.setOffline_icon_color(rs.getOfflineIconColor());
			    deviceBean.setStopped_icon_color(rs.getStoppedIconColor());
			    deviceBean.setEngine_idle_color(rs.getEngineIdleColor());
			    deviceBean.setImg_icon_type(rs.getImgIconType());
			    deviceBean.setImg_icon_name(rs.getImgIconName());

			    bean.setDeviceSetting(deviceBean);

			List<Events> eventList = eventRepo.findTop3ByDevice_IdAndAlertTimeBetweenOrderByAlertTimeDesc(
					rs.getDeviceId(), DateTimeHelper.stringToLocalDateTime(stime),
					DateTimeHelper.stringToLocalDateTime(etime));
			List<AlertDTO> dtoList = eventList.stream().map(event -> {
				AlertDTO dto = new AlertDTO();
				dto.setAlert_type(event.getAlertType());
				dto.setAlert_time(DateTimeHelper.utcToZone(event.getAlertTime(), user.getTimezone()));
                dto.setSpeed(event.getSpeed());    
				return dto;
			}).toList();

			bean.setEvents(dtoList);

			list.add(bean);
		}
		return list;
	}

	public LiveDataBean findLiveDataByDeviceId(long deviceId, Users user) {
		return liveDataRepo.findLiveDataByDeviceId(deviceId, user.getId()).map(p -> liveDataMapper.mapToBean(p, user))
				.orElse(null);
	}
	public LiveDataDto getLiveDataByDevice(
	        Users user, Long deviceId, String stime, String etime) {

	    LiveDataView rs =
	            liveDataRepo.findLiveDataByDevice(deviceId, stime, etime);

	    if (rs == null) {
	        return null;
	    }

	    LiveDataDto bean = new LiveDataDto();

	    bean.setDevice_id(rs.getDeviceId());
	    bean.setDevice_name(rs.getDeviceName());
	    bean.setObjectIcon(rs.getObjectIcon());
	    bean.setStatus(rs.getStatus());
	    bean.setVehicle_status(rs.getVehicleStatus());
	    bean.setAttributes(rs.getAttributes());
	    bean.setCourse(rs.getCourse());
	    bean.setAltitude(rs.getAltitude());
	    bean.setLatitude(rs.getLatitude());
	    bean.setLongitude(rs.getLongitude());
		bean.setTail(rs.getTailColor());
		bean.setTail_width(rs.getTailLength());
        bean.setAddress(rs.getAddress());
        bean.setUniqueid(rs.getUniqueid());
        bean.setSimCardNumber(rs.getSimCardNumber());
        bean.setModalType(rs.getModalType());
	    bean.setGroup_name(rs.getGroupName());
        bean.setDeviceModel(rs.getDeviceModel());
	    bean.setDevicetime(
	            DateTimeHelper.utcToZone(rs.getDeviceTime(), user.getTimezone()));
	    bean.setServertime(
	            DateTimeHelper.utcToZone(rs.getServerTime(), user.getTimezone()));
	    long currentTime = DateTimeUtil.getLocalUnixTime(System.currentTimeMillis() / 1000l, user.getTimezone());	
		LocalDateTime lastMovementLdt = rs.getLastMovementTime();
		LocalDateTime lastIdleLdt = rs.getLastIdleTime();

		if (lastMovementLdt != null) {
		    long lastMovementEpoch =
		            lastMovementLdt
		                    .atZone(ZoneId.of(user.getTimezone()))
		                    .toEpochSecond();

		    bean.setLastmovementtime(
		            DateTimeUtil.getTimeDiffInDays(
		                    (currentTime - lastMovementEpoch) * 1000L
		            )
		    );
		}

		if (lastIdleLdt != null) {
		    long lastIdleEpoch =
		            lastIdleLdt
		                    .atZone(ZoneId.of(user.getTimezone()))
		                    .toEpochSecond();

		    bean.setLastidletime(
		            DateTimeUtil.getTimeDiffInDays(
		                    (currentTime - lastIdleEpoch) * 1000L
		            )
		    );
		}

	    // Drivers
	    bean.setDrivers(driverRepo.findByDevice_Id(deviceId));

	    // Maintenance
	    bean.setServices(
	            maintenanceRepo.findTop1ByDevice_IdOrderByIdDesc(deviceId));

	    bean.setSpeed(rs.getSpeed());
	    DeviceSettingCustomBean deviceBean = new DeviceSettingCustomBean();
	    deviceBean.setIcon_type(rs.getIconType());
	    deviceBean.setMoving_icon_color(rs.getMovingIconColor());
	    deviceBean.setOffline_icon_color(rs.getOfflineIconColor());
	    deviceBean.setStopped_icon_color(rs.getStoppedIconColor());
	    deviceBean.setEngine_idle_color(rs.getEngineIdleColor());
	    deviceBean.setImg_icon_type(rs.getImgIconType());
	    deviceBean.setImg_icon_name(rs.getImgIconName());

	    bean.setDeviceSetting(deviceBean);

	    // Events (Top 3)
	    List<Events> eventList =
	            eventRepo.findTop3ByDevice_IdAndAlertTimeBetweenOrderByAlertTimeDesc(
	                    deviceId,
	                    DateTimeHelper.stringToLocalDateTime(stime),
	                    DateTimeHelper.stringToLocalDateTime(etime));

	    List<AlertDTO> dtoList = eventList.stream().map(event -> {
	        AlertDTO dto = new AlertDTO();
	        dto.setAlert_type(event.getAlertType());
	        dto.setAlert_time(
	                DateTimeHelper.utcToZone(event.getAlertTime(), user.getTimezone()));
	        return dto;
	    }).toList();

	    bean.setEvents(dtoList);

	    return bean;
	}


}
