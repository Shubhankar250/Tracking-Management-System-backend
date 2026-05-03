package com.trackingpath.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.configs.UploadConfig;
import com.trackingpath.dtos.BillingSummary;
import com.trackingpath.dtos.DeviceBean;
import com.trackingpath.dtos.DeviceInfo;
import com.trackingpath.dtos.DevicePlaybackDto;
import com.trackingpath.dtos.DevicesUpdateDto;
import com.trackingpath.dtos.DrivingPointCardDTO;
import com.trackingpath.dtos.EventDataBean;
import com.trackingpath.dtos.EventDataCardBean;
import com.trackingpath.dtos.EventDataDTO;
import com.trackingpath.dtos.FieldResult;
import com.trackingpath.dtos.GpsPoint;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;
import com.trackingpath.dtos.LatLng;
import com.trackingpath.dtos.LiveDataDTOForCard;
import com.trackingpath.dtos.ParkingDataBean;
import com.trackingpath.dtos.TodayActivityDTO;
import com.trackingpath.dtos.TractorWorkingDTO;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.DeviceGroupMapping;
import com.trackingpath.entities.DevicesUserMapping;
import com.trackingpath.entities.EventData;
import com.trackingpath.entities.GroupEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.DeviceMapper;
import com.trackingpath.repositories.DeviceGroupMappingRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DevicesUserMappingRepository;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.EventDataRepository;
import com.trackingpath.repositories.GroupRepository;
import com.trackingpath.repositories.UserRepository;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;
import com.trackingpath.util.Haversine;

import jakarta.transaction.Transactional;

@Service
public class DeviceService {

	private final UploadConfig uploadConfig;

	private final CommandService commandService;
	private final DeviceRepository deviceRepository;
	private final DeviceGroupMappingRepository deviceGroupMappingRepository;
	private final GroupRepository groupRepository;
	@Autowired
	private DevicesUserMappingRepository devicesUserMappingRepository;
	@Autowired
	DeviceMapper deviceMapper;
	@Autowired
	EventDataRepository eventDataRepository;
	@Autowired
	private UserRepository usersRepository;

	@Autowired
	private DriverRepository driverRepository;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public DeviceService(DeviceRepository deviceRepository, CommandService commandService, UploadConfig uploadConfig,
			DeviceGroupMappingRepository deviceGroupMappingRepository, GroupRepository groupRepository) {
		this.deviceRepository = deviceRepository;
		this.commandService = commandService;
		this.uploadConfig = uploadConfig;
		this.deviceGroupMappingRepository = deviceGroupMappingRepository;
		this.groupRepository = groupRepository;
	}

	public Map<Long, String> getDeviceIdNameMap(Long userId) {
		return deviceRepository.findIdAndNameByUserId(userId).stream().filter(r -> r[0] != null && r[1] != null)
				.collect(Collectors.toMap(r -> (Long) r[0], r -> (String) r[1]));
	}

	public Map<Long, String> getDevicesForUser(Users user) {

		Map<Long, String> result = new LinkedHashMap<>();

		if (user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
			List<DeviceEntity> devices = deviceRepository.findByUserIdAndVehicleStatus(user.getAdminId(), "ACTIVE");
			devices.forEach(d -> result.put(d.getId(), d.getName()));
		} else {
			String idsStr = user.getAssign_device_ids();
			if (idsStr != null && !idsStr.isBlank()) {
				List<Long> ids = Arrays.stream(idsStr.split(",")).map(String::trim).map(Long::parseLong).toList();

				List<DeviceEntity> devices = deviceRepository.findByIdInAndVehicleStatus(ids, "ACTIVE");
				devices.forEach(d -> result.put(d.getId(), d.getName()));
			}
		}

		return result;
	}

	public HistoryDataPlaybackDTO getPlaybackData(long deviceId, String startTime, String endTime, Users user,
			long time_interval, String type) {

		List<Object[]> rows = deviceRepository.getPlaybackData(deviceId, startTime, endTime);
		// System.out.println("ffff--" + rows.size());
		boolean isWorking = "WORKING".equalsIgnoreCase(type);

		ArrayList<LiveDataDTOForCard> liveDataBeanForCard = new ArrayList<>();
		ArrayList<EventDataDTO> movementDataList = new ArrayList<>();
		List<Object> combinedList = new ArrayList<>();
		List<Object> tripList = new ArrayList<>();
		DrivingPointCardDTO currentDriving = null;
		ParkingDataBean currentParking = null;
		double overall_distance = 0;
		double totalDistance = 0;
		double totalSpeed = 0;
		double maxSpeed = 0;
		int speedCount = 0;

		boolean inDriving = false, inParking = false;
		double lastLat = 0, lastLng = 0;
		long drivingStartTime = 0, parkingStartTime = 0;
		long lastDeviceTime = 0;
		boolean isIdleForOverall = false, isRunningForOverall = false;
		long idleStartTime = 0, runningStartTime = 0;
		long totalRunningTime = 0, totalIdleTime = 0, lastDeviceTimeForOverall = 0;
		double totalSpeedForOverall = 0, maxSpeedForOverall = 0;
		int speedCountForOverall = 0;
		double lastLatForOverall = 0, lastLngForOverall = 0;

		String lastFormattedDeviceTimeStr = null;
		List<GpsPoint> points = new ArrayList<>();
		for (Object[] r : rows) {
			// Corrected indexes according to query
			double lat = r[9] != null ? ((Number) r[9]).doubleValue() : 0;
			double lon = r[10] != null ? ((Number) r[10]).doubleValue() : 0;

			if (lat == 0 || lon == 0) {
				continue;
			}

			long deviceTime = r[7] != null ? ((Number) r[7]).longValue() : 0;
			long fixTime = r[8] != null ? ((Number) r[8]).longValue() : 0;
			long serverTime = r[13] != null ? ((Number) r[13]).longValue() : 0;

			double speed = r[14] != null ? ((Number) r[14]).doubleValue() : 0;
			String address = r[1] != null ? r[1].toString() : "";
			String alertName = r[16] != null ? r[16].toString() : null;
			String name = r[11] != null ? r[11].toString() : "";
			String deviceTimezone = r[18] != null ? r[18].toString() : null;

			lastDeviceTime = deviceTime;
			lastDeviceTimeForOverall = deviceTime;

			// Convert epoch → UTC LocalDateTime
			LocalDateTime utcDeviceDateTime = Instant.ofEpochSecond(deviceTime).atZone(ZoneOffset.UTC)
					.toLocalDateTime();

			LocalDateTime utcFixDateTime = Instant.ofEpochSecond(fixTime).atZone(ZoneOffset.UTC).toLocalDateTime();

			LocalDateTime utcServerDateTime = Instant.ofEpochSecond(serverTime).atZone(ZoneOffset.UTC)
					.toLocalDateTime();

			LocalDateTime formattedDeviceTime;
			LocalDateTime formattedFixTime;
			LocalDateTime formattedServerTime;

			formattedDeviceTime = DateTimeHelper.convertToUserZone(utcDeviceDateTime, deviceTimezone,
					user.getTimezone());

			formattedFixTime = DateTimeHelper.convertToUserZone(utcFixDateTime, deviceTimezone, user.getTimezone());

			formattedServerTime = DateTimeHelper.convertToUserZone(utcServerDateTime, deviceTimezone,
					user.getTimezone());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

			String formattedDeviceTimeStr = formattedDeviceTime.format(formatter);
			String formattedFixTimeStr = formattedFixTime.format(formatter);
			String formattedServerTimeStr = formattedServerTime.format(formatter);
			lastFormattedDeviceTimeStr = formattedDeviceTimeStr;

			GpsPoint gpsData = new GpsPoint();
			gpsData.setDeviceTime(formattedDeviceTime);
			gpsData.setDeviceId(deviceId);
			gpsData.setLatitude(lat);
			gpsData.setLongitude(lon);
			gpsData.setSpeed(speed);
			points.add(gpsData);

			if (speed >= 0) {
				totalSpeedForOverall += speed;
				speedCountForOverall++;
				maxSpeedForOverall = Math.max(maxSpeedForOverall, speed);
			}

			// Running / idle tracking
			if (speed == 0) {
				if (!isIdleForOverall) {
					idleStartTime = deviceTime;
					isIdleForOverall = true;
				}
				if (isRunningForOverall) {
					totalRunningTime += deviceTime - runningStartTime;
					isRunningForOverall = false;
				}
			} else {
				if (!isRunningForOverall) {
					runningStartTime = deviceTime;
					isRunningForOverall = true;
				}
				if (isIdleForOverall) {
					totalIdleTime += deviceTime - idleStartTime;
					isIdleForOverall = false;
				}
			}

			// Driving / Parking segments
			if (speed == 0) {
				// End current driving segment
				if (inDriving) {
					currentDriving.setMovment_end_time(formattedDeviceTimeStr);
					currentDriving.setSeg_max_speed(maxSpeed);
					currentDriving.setSeg_avg_speed(speedCount > 0
							? new BigDecimal(totalSpeed / speedCount).setScale(2, RoundingMode.HALF_UP).doubleValue()
							: 0);
					currentDriving.setSeg_distance(
							new BigDecimal(totalDistance).setScale(2, RoundingMode.HALF_UP).doubleValue());
					currentDriving.setSeg_running_time(
							DateTimeUtil.getTimeDiffInDays((deviceTime - drivingStartTime) * 1000));
					combinedList.add(currentDriving);
					currentDriving = null;
					maxSpeed = 0;
					totalSpeed = 0;
					speedCount = 0;
					totalDistance = 0;
					inDriving = false;
				}

				// Start parking
				if (!inParking) {
					inParking = true;
					parkingStartTime = deviceTime;

					currentParking = new ParkingDataBean();
					currentParking.setParking_start_time(formattedDeviceTimeStr);
					currentParking.setAddress(address);
					currentParking.setName(name);
					currentParking.setLat(lat);
					currentParking.setLon(lon);
				}

			} else { // speed > 0
				// End parking
				if (inParking) {
					currentParking.setParking_end_time(formattedDeviceTimeStr);
					long duration = deviceTime - parkingStartTime;
					currentParking.setTime(DateTimeUtil.getTimeDiffInDays(duration * 1000));
					if (duration > time_interval) {
						combinedList.add(currentParking);
					}
					currentParking = null;
					inParking = false;
				}

				// Start driving
				if (!inDriving) {
					inDriving = true;
					drivingStartTime = deviceTime;

					currentDriving = new DrivingPointCardDTO();
					currentDriving.setSeg_address(address);
					currentDriving.setMovment_start_time(formattedDeviceTimeStr);
				}

				// Distance calculation
				if (lastLat != 0 && lastLng != 0) {
					double dist = Haversine.getDistanceInKm(lat, lon, lastLat, lastLng);
					totalDistance += dist;
				}

				// Driving stats
				totalSpeed += speed;
				speedCount++;
				if (speed > maxSpeed)
					maxSpeed = speed;

				// Polyline points
				currentDriving.getPolylinePoints().add(new LatLng(lat, lon));
				lastLat = lat;
				lastLng = lon;
			}

			// Event data
			if (alertName != null) {
				EventDataCardBean event = new EventDataCardBean();
				event.setEvent_name(alertName);
				event.setEvent_time(formattedDeviceTimeStr);
				event.setLatitude(lat);
				event.setLongitude(lon);
				event.setAddress(address);
				event.setSpeed(speed);
				combinedList.add(event);
			}

			// Movement data for card
			EventDataDTO data = new EventDataDTO();
			DevicePlaybackDto device = new DevicePlaybackDto();
			device.setName(name);
			data.setAddress(address);
			data.setAttributes(r[3] != null ? r[3].toString() : "");
			data.setAltitude(r[2] != null ? ((Number) r[2]).doubleValue() : 0);
			data.setCourse(r[4] != null ? ((Number) r[4]).doubleValue() : 0);
			data.setDeviceId(r[0] != null ? ((Number) r[0]).longValue() : 0);
			data.setFuelLevel(r[5] != null ? ((Number) r[5]).doubleValue() : 0);
			data.setDeviceTime(formattedDeviceTimeStr);
			data.setFixTime(formattedFixTimeStr);
			data.setLatitude(lat);
			data.setLongitude(lon);
			data.setServerTime(formattedServerTimeStr);
			data.setSpeed(speed);
			data.setDetails(device);
			data.setDataServerTime(serverTime);
			movementDataList.add(data);

			// Overall distance
			if (lastLatForOverall != 0 && lastLngForOverall != 0) {
				overall_distance += Haversine.getDistanceInKm(lat, lon, lastLatForOverall, lastLngForOverall);
			}
			lastLatForOverall = lat;
			lastLngForOverall = lon;
		}

		// Close any open segments after loop
		if (inDriving && currentDriving != null) {
			currentDriving.setMovment_end_time(lastFormattedDeviceTimeStr);
			currentDriving.setSeg_max_speed(maxSpeed);
			currentDriving.setSeg_avg_speed(speedCount > 0
					? new BigDecimal(totalSpeed / speedCount).setScale(2, RoundingMode.HALF_UP).doubleValue()
					: 0);
			currentDriving
					.setSeg_distance(new BigDecimal(totalDistance).setScale(2, RoundingMode.HALF_UP).doubleValue());
			currentDriving
					.setSeg_running_time(DateTimeUtil.getTimeDiffInDays((lastDeviceTime - drivingStartTime) * 1000));
			combinedList.add(currentDriving);
		}

		if (inParking && currentParking != null) {
			currentParking.setParking_end_time(lastFormattedDeviceTimeStr);
			long duration = lastDeviceTime - parkingStartTime;
			currentParking.setTime(DateTimeUtil.getTimeDiffInDays(duration * 1000));
			if (duration > time_interval) {
				combinedList.add(currentParking);
			}
		}

		// Final overall idle/running time
		if (isIdleForOverall)
			totalIdleTime += lastDeviceTimeForOverall - idleStartTime;
		if (isRunningForOverall)
			totalRunningTime += lastDeviceTimeForOverall - runningStartTime;
		// System.out.println("totalSpeedForOverall
		// "+totalSpeedForOverall+"speedCountForOverall "+speedCountForOverall);
		double avgSpeedForOverall = speedCountForOverall > 0
				? new BigDecimal(totalSpeedForOverall / speedCountForOverall).setScale(2, RoundingMode.HALF_UP)
						.doubleValue()
				: 0;

		LiveDataDTOForCard liveData = new LiveDataDTOForCard();
		liveData.setTotal_running_time(DateTimeUtil.getTimeDiffInDays(totalRunningTime * 1000));
		liveData.setIdle_time(DateTimeUtil.getTimeDiffInDays(totalIdleTime * 1000));
		liveData.setAverage_speed(avgSpeedForOverall);
		liveData.setMax_speed(maxSpeedForOverall);
		liveDataBeanForCard.add(liveData);
		BillingSummary summary = null;
		if (isWorking) {
			try {
				TractorBillingCalculator calculator = new TractorBillingCalculator();
				summary = calculator.calculate(points, 2.0);
			} catch (Exception e) {
				e.printStackTrace(); // or use logger
			}
		}

		HistoryDataPlaybackDTO historyDataBean = new HistoryDataPlaybackDTO();
		historyDataBean.setEventDataList(movementDataList);
		historyDataBean.setCombinedPlaybackList(combinedList);
		historyDataBean.setLiveDataBeanForCard(liveDataBeanForCard);
		historyDataBean.setSummary(summary);
		historyDataBean.setDistance(new BigDecimal(overall_distance).setScale(2, RoundingMode.HALF_UP).doubleValue());

		return historyDataBean;

	}

	public DevicesUpdateDto getDeviceByIdForUpdate(Long deviceId, Users user) {

		boolean isAdmin = user.getRoles() != null
				&& user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

		if (isAdmin) {
			return deviceRepository.findDeviceForAdmin(deviceId).orElse(null);
		}

		return deviceRepository.findDeviceForUser(deviceId, user.getId()).orElse(null);
	}

	@Transactional
	public boolean updateDevicesData(DevicesUpdateDto bean, Users currentUser) {

		DeviceEntity device = deviceRepository.findById(bean.getDeviceId())
				.orElseThrow(() -> new RuntimeException("Device not found"));

		// ---------------- Update Device Fields ----------------

		if (hasText(bean.getDeviceName()))
			device.setName(bean.getDeviceName());

		if (hasText(bean.getObjectIcon()))
			device.setObjectIcon(bean.getObjectIcon());

		if (hasText(bean.getDeviceTimezone()))
			device.setDevicetimezone(bean.getDeviceTimezone());

		if (hasText(bean.getDeviceModel()))
			device.setDeviceModel(bean.getDeviceModel());

		if (hasText(bean.getDeviceImei()))
			device.setUniqueid(bean.getDeviceImei());

		if (hasText(bean.getSimCardNumber()))
			device.setSimCardNumber(bean.getSimCardNumber());

		if (hasText(bean.getVin()))
			device.setVin(bean.getVin());
		
		if (bean.getSimActivationDate() != null) {
			device.setSimActivationDate(bean.getSimActivationDate());
		}

		if (bean.getSimExpirationDate() != null) {
			device.setSimExpirationDate(bean.getSimExpirationDate());
		}

		if (bean.getInstallationDate() != null) {
			device.setInstallationDate(bean.getInstallationDate());
		}

		if (hasText(bean.getPlateNumber()))
			device.setPlateNumber(bean.getPlateNumber());

		if (hasText(bean.getRegistrationNumber()))
			device.setRegistrationNumber(bean.getRegistrationNumber());

		if (hasText(bean.getOwner()))
			device.setOwner(bean.getOwner());

		if (hasText(bean.getFuelMeasureName()))
			device.setFuelMeasureName(bean.getFuelMeasureName());

		if (bean.getFuelMeasurement() != null)
			device.setFuelMeasurement(bean.getFuelMeasurement());

		if (bean.getFuelCost() != null)
			device.setFuelCost(bean.getFuelCost());

		if (hasText(bean.getIconType()))
			device.setIconType(bean.getIconType());

		if (hasText(bean.getMovingIconColor()))
			device.setMovingIconColor(bean.getMovingIconColor());

		if (hasText(bean.getStoppedIconColor()))
			device.setStoppedIconColor(bean.getStoppedIconColor());

		if (hasText(bean.getOfflineIconColor()))
			device.setOfflineIconColor(bean.getOfflineIconColor());

		if (hasText(bean.getEngineIdleColor()))
			device.setEngineIdleColor(bean.getEngineIdleColor());

		if (hasText(bean.getTailColor()))
			device.setTailColor(bean.getTailColor());

		if (bean.getTailLength() != null)
			device.setTailLength(bean.getTailLength());

		if (hasText(bean.getImgIconType()))
			device.setImgIconType(bean.getImgIconType());

		if (hasText(bean.getImgIconName()))
			device.setImgIconName(bean.getImgIconName());

		if (bean.getOdometer() != null)
			device.setOdometer(bean.getOdometer());

		if (hasText(bean.getVehicleStatus()))
			device.setVehicleStatus(bean.getVehicleStatus());

		if (hasText(bean.getMaxSpeed()))
			device.setMaxSpeed(bean.getMaxSpeed());

		if (hasText(bean.getMinMovingSpeed()))
			device.setMinMovingSpeed(bean.getMinMovingSpeed());

		if (hasText(bean.getMinFuelFillings()))
			device.setMinFuelFillings(bean.getMinFuelFillings());

		if (hasText(bean.getMinFuelTheft()))
			device.setMinFuelTheft(bean.getMinFuelTheft());

		if (hasText(bean.getFuelChangeAfterStop()))
			device.setFuelChangeAfterStop(bean.getFuelChangeAfterStop());
		if (hasText(bean.getRcPath()))
			device.setRcPath(bean.getRcPath());

		if (hasText(bean.getInsurancePath()))
			device.setInsurancePath(bean.getInsurancePath());
		deviceRepository.save(device);
		// ---------------- Update DeviceGroupMapping ----------------

		Long groupId = (bean.getGroupId() != null && bean.getGroupId() > 0) ? bean.getGroupId() : 1L;

		Long userId = (bean.getUserId() != null && bean.getUserId() > 0) ? bean.getUserId() : currentUser.getId();

		DeviceGroupMapping groupMapping = deviceGroupMappingRepository.findByDeviceIdAndUserId(device.getId(), userId)
				.orElse(null);

		if (groupMapping != null) {

			// Update existing mapping
			if (!groupId.equals(groupMapping.getGroup().getId())) {

				GroupEntity newGroup = groupRepository.findById(groupId)
						.orElseThrow(() -> new RuntimeException("Group not found"));

				groupMapping.setGroup(newGroup);
			}

			groupMapping.setUserId(userId);

		} else {

			// Create mapping if it does not exist
			groupMapping = new DeviceGroupMapping();
			groupMapping.setDevice(device);

			GroupEntity newGroup = groupRepository.findById(groupId)
					.orElseThrow(() -> new RuntimeException("Group not found"));

			groupMapping.setGroup(newGroup);

			groupMapping.setUserId(userId);
			groupMapping.setAdminId(currentUser.getAdminId());
			groupMapping.setCreationTime(LocalDateTime.now());
		}

		deviceGroupMappingRepository.save(groupMapping);

		// ---------------- Update DevicesUserMapping ----------------

		DevicesUserMapping userMapping = devicesUserMappingRepository.findByDeviceId(device.getId()).orElse(null);

		if (userMapping != null) {

			// Update existing
			if (!userId.equals(userMapping.getUser().getId())) {

				Users newUser = usersRepository.getReferenceById(userId);
				userMapping.setUser(newUser);
			}

		} else {

			// Create new mapping
			userMapping = new DevicesUserMapping();
			userMapping.setDevice(device);
			userMapping.setAdmin(currentUser);

			Users newUser = usersRepository.getReferenceById(userId);
			userMapping.setUser(newUser);
		}

		devicesUserMappingRepository.save(userMapping);

		return true;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public DeviceInfo getDeviceById(long deviceId) {

		long currentTime = DateTimeUtil.getLocalUnixTime(System.currentTimeMillis() / 1000L, "Asia/Kolkata");

		return deviceRepository.findDeviceInfo(deviceId).map(p -> {

			String lastMovementDiff = null;
			String lastIdleDiff = null;

			if (p.getLastmovementtime() != null && p.getLastmovementtime() > 0) {
				lastMovementDiff = DateTimeUtil.getTimeDiffInDays((currentTime - p.getLastmovementtime()) * 1000L);
			}

			if (p.getLastidletime() != null && p.getLastidletime() > 0) {
				lastIdleDiff = DateTimeUtil.getTimeDiffInDays((currentTime - p.getLastidletime()) * 1000L);
			}

			return new DeviceInfo(p.getIconType(), p.getImgIconName(), p.getName(), p.getStatus(), lastIdleDiff,
					lastMovementDiff, p.getDevicetimezone()

			);
		}).orElse(new DeviceInfo(null, null, null, null, null, null, null));
	}

	public List<EventDataBean> getPlaybackDrivingData(Long deviceId, String start_time, String end_time, Users user) {

		// Convert User Time → UTC
		start_time = DateTimeUtil.convertUserTimeToUTC(start_time, user.getTimezone());
		end_time = DateTimeUtil.convertUserTimeToUTC(end_time, user.getTimezone());

		DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		LocalDateTime startTime = LocalDateTime.parse(start_time, dbFormatter);
		LocalDateTime endTime = LocalDateTime.parse(end_time, dbFormatter);

		List<EventData> events = eventDataRepository.getPlaybackDataForDrivingData(deviceId, startTime, endTime);

		ZoneId userZone = ZoneId.of(user.getTimezone());

		// ✅ Required Output Format
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

		List<EventDataBean> eventList = new ArrayList<>();

		for (EventData event : events) {

			EventDataBean data = new EventDataBean();
			DeviceBean device = new DeviceBean();

			data.setAddress(event.getAddress());
			data.setAttributes(event.getAttributes());
			data.setAltitude(event.getAltitude());
			data.setCourse(event.getCourse());
			data.setDeviceId(event.getDeviceId());
			data.setFuelLevel(event.getFuelLevel() != null ? event.getFuelLevel() : 0.0);

			// ✅ Convert UTC → User Local Time + Format
			data.setDeviceTime(event.getDeviceTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(userZone)
					.format(outputFormatter));

			data.setFixTime(
					event.getFixTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(userZone).format(outputFormatter));

			data.setServerTime(event.getServerTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(userZone)
					.format(outputFormatter));

			data.setLatitude(event.getLatitude());
			data.setLongitude(event.getLongitude());
			data.setSpeed(event.getSpeed());
			data.setDetails(device);

			data.setDataServerTime(event.getServerTime().atZone(ZoneId.of("UTC")).toEpochSecond());

			eventList.add(data);
		}

		return eventList;
	}

	public TodayActivityDTO getTodayactivity(Users user, Long deviceId, LocalDateTime stime, LocalDateTime etime) {

		List<Object[]> rows = deviceRepository.getTodayactivity(deviceId, stime, etime);

		TodayActivityDTO dto = new TodayActivityDTO();

		if (rows == null || rows.size() < 2) {
			dto.setTotalRunningTime("00:00:00");
			dto.setTotalIdleTime("00:00:00");
			dto.setTotalStopTime("00:00:00");
			dto.setWorkingHours("00:00:00");
			dto.setWorkStartTime("-");
			dto.setWorkEndTime("-");
			return dto;
		}
		double totalDistance = 0;
		double lastLat = 0, lastLng = 0;
		Timestamp lastTime = null;
		long runningSeconds = 0;
		long idleSeconds = 0;
		long stopSeconds = 0;

		Timestamp workStart = null;
		Timestamp workEnd = null;

		for (int i = 0; i < rows.size() - 1; i++) {

			Object[] current = rows.get(i);
			Object[] next = rows.get(i + 1);

			String attributes = current[0] != null ? current[0].toString() : "";

			Timestamp currentTime = (Timestamp) current[5]; // ✅ correct
			Timestamp nextTime = (Timestamp) next[5];

			double speed = current[2] != null ? ((Number) current[2]).doubleValue() : 0;

			double lat = current[3] != null ? ((Number) current[3]).doubleValue() : 0;
			double lon = current[4] != null ? ((Number) current[4]).doubleValue() : 0;

			long diffSeconds = (nextTime.getTime() - currentTime.getTime()) / 1000;
			if (diffSeconds < 0)
				diffSeconds = 0;

			boolean ignitionOn = attributes.contains("\"ignition\":true");

			// 🔥 DISTANCE
			if (lastLat != 0 && lastLng != 0 && lat != 0 && lon != 0 && lastTime != null) {

				double dist = Haversine.getDistanceInKm(lat, lon, lastLat, lastLng);
				long timeDiff = (currentTime.getTime() - lastTime.getTime()) / 1000;

				if (timeDiff <= 300 && dist < 2 && speed > 0) {
					totalDistance += dist;
				}
			}

			// ✅ IMPORTANT: update last values
			lastLat = lat;
			lastLng = lon;
			lastTime = currentTime;

			// RUNNING
			if (speed > 0) {
				runningSeconds += diffSeconds;
			}
			// IDLE
			else if (ignitionOn) {
				idleSeconds += diffSeconds;
			}
			// STOP
			else {
				stopSeconds += diffSeconds;
			}

			// Work Start
			if (workStart == null && (speed > 0 || ignitionOn)) {
				workStart = currentTime;
			}

			// Work End
			if (speed > 0 || ignitionOn) {
				workEnd = nextTime;
			}
		}

		dto.setTotalRunningTime(formatDuration(runningSeconds));
		dto.setTotalIdleTime(formatDuration(idleSeconds));
		dto.setTotalStopTime(formatDuration(stopSeconds));
		dto.setWorkingHours(formatDuration(runningSeconds + idleSeconds));
		dto.setTotalDistance(Math.round(totalDistance * 100.0) / 100.0);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		dto.setWorkStartTime(
				workStart != null
						? DateTimeHelper.convertUtcToUser(workStart.toLocalDateTime(), user.getTimezone()).toLocalTime()
								.format(formatter)
						: "-");

		dto.setWorkEndTime(
				workEnd != null
						? DateTimeHelper.convertUtcToUser(workEnd.toLocalDateTime(), user.getTimezone()).toLocalTime()
								.format(formatter)
						: "-");

		return dto;
	}

	private String formatDuration(long totalSeconds) {

		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public String findTimezoneByDeviceId(Long deviceId) {
		String deviceTimezone = deviceRepository.findTimezoneByDeviceId(deviceId);
		return deviceTimezone;
	}

	public Map<Long, String> notAssignAnyDriver(Users user) {

		Map<Long, String> result = new LinkedHashMap<>();

		// 🔴 Step 1: Get already assigned device IDs
		List<Long> assignedDeviceIds = driverRepository.findAssignedDeviceIds();

		List<DeviceEntity> devices = new ArrayList<>();

		if (user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {

			devices = deviceRepository.findByUserIdAndVehicleStatus(user.getAdminId(), "ACTIVE");

		} else {

			String idsStr = user.getAssign_device_ids();

			if (idsStr != null && !idsStr.isBlank()) {

				List<Long> ids = Arrays.stream(idsStr.split(",")).map(String::trim).map(Long::parseLong).toList();

				devices = deviceRepository.findByIdInAndVehicleStatus(ids, "ACTIVE");
			}
		}

		// 🔴 Step 2: Remove already assigned devices
		devices.stream().filter(d -> !assignedDeviceIds.contains(d.getId()))
				.forEach(d -> result.put(d.getId(), d.getName()));

		return result;
	}

}
