package com.trackingpath.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.trackingpath.websocket.LiveWebSocketHandler;
import org.springframework.stereotype.Component;

import com.trackingpath.dtos.MaintenanceAllDataView;
import com.trackingpath.dtos.MaintenanceDto;
import com.trackingpath.dtos.MaintenanceResponseDto;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.MaintenanceEntity;
import com.trackingpath.entities.Users;

@Component
public class MaintenanceMapper {

	private final LiveWebSocketHandler liveWebSocketHandler;

	MaintenanceMapper(LiveWebSocketHandler liveWebSocketHandler) {
		this.liveWebSocketHandler = liveWebSocketHandler;
	}

	public List<MaintenanceResponseDto> toDtoList(List<MaintenanceEntity> data) {
		return data.stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<MaintenanceResponseDto> toDtoListView(List<MaintenanceAllDataView> data) {
		return data.stream().map(this::toDtoForView).collect(Collectors.toList());
	}

	private MaintenanceResponseDto toDtoForView(MaintenanceAllDataView v) {
		MaintenanceResponseDto dto = new MaintenanceResponseDto();

		dto.setId(v.getId());
		dto.setServiceName(v.getServiceName());
		dto.setDeviceId(v.getDeviceId());
		dto.setDevice_name(v.getDeviceName());
		dto.setOdometerIntervalKmVal(v.getOdometerIntervalKmVal());
		dto.setOdometerLeftKmVal(v.getOdometerLeftKmVal());
		dto.setEngineHourIntervalVal(v.getEngineHourIntervalVal());
		dto.setEngineHoursLeftVal(v.getEngineHoursLeftVal());
		dto.setLastServiceHours(v.getLastServiceHours());
		dto.setDaysIntervalVal(v.getDaysIntervalVal());
		dto.setDaysLeftVal(v.getDaysLeftVal());
		dto.setEventTrigger(v.getEventTrigger());
		dto.setDatalist(Boolean.TRUE.equals(v.getDatalist()));
		dto.setPopup(Boolean.TRUE.equals(v.getPopup()));
        dto.setAdminId(v.getAdminId());
        dto.setUserId(v.getUserId());
		dto.setOdometerIntervalKm(v.getOdometerIntervalKm());
		dto.setLastServiceKm(v.getLastServiceKm());
		
		  dto.setLastServiceDate( v.getLastServiceDate() != null ?
		  v.getLastServiceDate().toString() : null );
		 
		dto.setUsername(v.getUsername());
		dto.setAdmin_name(v.getAdminName());

		return dto;
	}

	public MaintenanceResponseDto toDto(MaintenanceEntity e) {

		MaintenanceResponseDto dto = new MaintenanceResponseDto();

		dto.setId(e.getId());
		dto.setServiceName(e.getServiceName());

		dto.setDevice_name(e.getDevice().getName());
       dto.setDeviceId(e.getDevice().getId());
		dto.setDatalist(Boolean.TRUE.equals(e.getDatalist()));
		dto.setPopup(Boolean.TRUE.equals(e.getPopup()));

		dto.setOdometerIntervalKm(Boolean.TRUE.equals(e.getOdometerIntervalKm()));
		dto.setOdometerIntervalKmVal(e.getOdometerIntervalKmVal());
		dto.setLastServiceKm(e.getLastServiceKm());

		dto.setEngineHourInterval(Boolean.TRUE.equals(e.getEngineHourInterval()));
		dto.setEngineHourIntervalVal(e.getEngineHourIntervalVal());
		dto.setLastServiceHours(e.getLastServiceHours());

		dto.setDaysInterval(Boolean.TRUE.equals(e.getDaysInterval()));
		dto.setDaysIntervalVal(e.getDaysIntervalVal());

		dto.setOdometerLeftKm(Boolean.TRUE.equals(e.getOdometerLeftKm()));
		dto.setOdometerLeftKmVal(e.getOdometerLeftKmVal());

		dto.setEngineHoursLeft(Boolean.TRUE.equals(e.getEngineHoursLeft()));
		dto.setEngineHoursLeftVal(e.getEngineHoursLeftVal());

		dto.setUpdateLastService(Boolean.TRUE.equals(e.getUpdateLastService()));
		dto.setDaysLeft(Boolean.TRUE.equals(e.getDaysLeft()));
		dto.setDaysLeftVal(e.getDaysLeftVal());

		dto.setEventTrigger(Boolean.TRUE.equals(e.getEventTrigger()));

		// date
		if (e.getLastServiceDate() != null) {
			dto.setLastServiceDate(e.getLastServiceDate().toString());
		}

		// users
		if (e.getUser() != null) {
			dto.setUserId(e.getUser().getId());
			dto.setUsername(e.getUser().getUsername());
		}

		if (e.getAdmin() != null) {
			dto.setAdminId(e.getAdmin().getId());
			dto.setAdmin_name(e.getAdmin().getUsername());
		}

		return dto;
	}

	// ------------------------------- // DTO → ENTITY //

	public static MaintenanceEntity toEntity(MaintenanceDto dto, DeviceEntity device, Users user, Users admin) {
		MaintenanceEntity entity = new MaintenanceEntity();

		entity.setServiceName(dto.getServiceName());
		entity.setDevice(device);

		entity.setDatalist(dto.isDatalist());
		entity.setPopup(dto.isPopup());

		entity.setOdometerIntervalKm(dto.isOdometerIntervalKm());
		entity.setOdometerIntervalKmVal(dto.getOdometerIntervalKmVal());
		entity.setLastServiceKm(dto.getLastServiceKm());

		entity.setEngineHourInterval(dto.isEngineHourInterval());
		entity.setEngineHourIntervalVal(dto.getEngineHourIntervalVal());
		entity.setLastServiceHours(dto.getLastServiceHours());

		entity.setDaysInterval(dto.isDaysInterval());
		entity.setDaysIntervalVal(dto.getDaysIntervalVal());

		entity.setOdometerLeftKm(dto.isOdometerLeftKm());
		entity.setOdometerLeftKmVal(dto.getOdometerLeftKmVal());

		entity.setEngineHoursLeft(dto.isEngineHoursLeft());
		entity.setEngineHoursLeftVal(dto.getEngineHoursLeftVal());

		entity.setUpdateLastService(dto.isUpdateLastService());
		entity.setDaysLeft(dto.isDaysLeft());
		entity.setDaysLeftVal(dto.getDaysLeftVal());

		entity.setEventTrigger(dto.isEventTrigger());

		if (dto.getLastServiceDate() != null && !dto.getLastServiceDate().isEmpty()) {
			entity.setLastServiceDate(LocalDate.parse(dto.getLastServiceDate()));
		}

		entity.setUser(user);
		entity.setAdmin(admin);

		return entity;
	}

}
