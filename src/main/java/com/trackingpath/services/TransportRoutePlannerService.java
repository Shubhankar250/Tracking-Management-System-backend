package com.trackingpath.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.trackingpath.dtos.PassengerAssignmentDto;
import com.trackingpath.dtos.RoutePlannerRequest;
import com.trackingpath.dtos.RoutePlannerResponse;
import com.trackingpath.dtos.StopDto;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.entities.TransportRoute;
import com.trackingpath.entities.TransportRouteStop;
import com.trackingpath.entities.TransportShift;
import com.trackingpath.entities.TransportShiftHoliday;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.RoleRepository;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;
import com.trackingpath.repositories.TransportRouteRepository;
import com.trackingpath.repositories.TransportRouteStopRepository;
import com.trackingpath.repositories.TransportShiftHolidayRepository;
import com.trackingpath.repositories.TransportShiftRepository;

import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportRoutePlannerService {
	private final TransportShiftRepository shiftRepository;
	private final TransportShiftHolidayRepository shiftHolidayRepository;
	private final TransportRouteRepository routeRepository;
	private final TransportRouteStopRepository stopRepository;
	private final TransportPassengerRepository passengerRepository;
	private final TransportPassengerRouteAssignmentRepository passengerRouteAssignmentRepository;
	private final PasswordEncoder passwordEncoder;
	@Value("${FILE_SOUND_UPLOAD_PATH}")
	private String basePath;

	
	@Transactional
	public RoutePlannerResponse save(RoutePlannerRequest request, List<MultipartFile> files) {

		TransportRoute route = new TransportRoute();
		TransportShift shift = new TransportShift();

		// ===== SHIFT =====
		shift.setShiftName(request.getShiftName());
		shift.setStartTime(LocalTime.parse(request.getStartTime()));
		shift.setEndTime(LocalTime.parse(request.getEndTime()));
		shift.setActiveDaysMask(String.join(",", request.getActiveDays()));
		shift.setActive(true);
		shift = shiftRepository.save(shift);

		for (LocalDate holiday : request.getHolidayDates()) {
			TransportShiftHoliday sh = new TransportShiftHoliday();
			sh.setShift(shift);
			sh.setHolidayDate(holiday);
			shiftHolidayRepository.save(sh);
		}

		// ===== ROUTE =====
		route.setShift(shift);
		route.setRouteName(request.getRouteName());
		route.setRouteType(request.getRouteType());
		route.setDefaultVehicleId(request.getDefaultVehicleId());
		route.setSourceType(request.getSourceType());
		route.setRouteGeoJson(request.getRouteGeoJson());
		route.setActive(true);
		route = routeRepository.save(route);

		// ===== STOPS =====
		List<MultipartFile> safeFiles = files != null ? files : new ArrayList<>();

		for (int i = 0; i < request.getStops().size(); i++) {

			StopDto dto = request.getStops().get(i);
			MultipartFile file = i < safeFiles.size() ? safeFiles.get(i) : null;

			TransportRouteStop stop = new TransportRouteStop();
			stop.setRoute(route);
			stop.setSequenceNo(dto.getSequenceNo());
			stop.setStopName(dto.getStopName());
			stop.setLatitude(dto.getLatitude());
			stop.setLongitude(dto.getLongitude());
			stop.setStopType(dto.getStopType());
			stop.setGeofenceRadius(dto.getGeofenceRadius());
			stop.setAutoDetected(Boolean.TRUE.equals(dto.getAutoDetected()));
			stop.setApproved(Boolean.TRUE.equals(dto.getApproved()));
			stop.setPassengerCount(dto.getPassengerCount() == null ? 0 : dto.getPassengerCount());
			stop.setClientStopId(dto.getClientStopId());

			if (file != null && !file.isEmpty()) {
				String fileName = storeFile(file, request.getRouteName(), dto.getStopName());
				stop.setAnnouncementFile(fileName);
			}

			stopRepository.save(stop);
		}

		// ===== PASSENGERS =====
		for (PassengerAssignmentDto dto : request.getPassengers()) {

			TransportPassenger passenger = new TransportPassenger();
			passenger.setCreatedAt(LocalDateTime.now());

			passenger.setPassengerName(dto.getPassengerName());
			passenger.setPassengerType(dto.getPassengerType() == null ? "Student" : dto.getPassengerType());
			passenger.setGuardianName(dto.getGuardianName());
			passenger.setGuardianMobile(dto.getGuardianMobile());
			passenger.setActive(dto.getActive() == null ? true : dto.getActive());
			passenger.setUpdatedAt(LocalDateTime.now());
			passenger = passengerRepository.save(passenger);

			TransportPassengerRouteAssignment assignment = new TransportPassengerRouteAssignment();
			assignment.setPassenger(passenger);
			assignment.setRoute(route);

			if (dto.getPickupStopId() != null) {
				assignment
						.setPickupStop(stopRepository.findByRouteIdAndClientStopId(route.getId(), dto.getPickupStopId())
								.orElseThrow(() -> new IllegalArgumentException("Pickup stop not found")));
			}

			if (dto.getDropStopId() != null) {
				assignment.setDropStop(stopRepository.findByRouteIdAndClientStopId(route.getId(), dto.getDropStopId())
						.orElseThrow(() -> new IllegalArgumentException("Drop stop not found")));
			}

			assignment.setAutoLoginEnabled(Boolean.TRUE.equals(dto.getAutoLoginEnabled()));
			assignment.setUsername(dto.getUsername());
			assignment.setTempPassword(passwordEncoder.encode(dto.getTempPassword()));
			assignment.setActive(dto.getActive() == null ? true : dto.getActive());
			assignment.setCreatedAt(LocalDateTime.now());
			assignment.setUpdatedAt(LocalDateTime.now());

			passengerRouteAssignmentRepository.save(assignment);
		}

		return getRoute(route.getId());
	}

	@Transactional
	public RoutePlannerResponse update(RoutePlannerRequest request, List<MultipartFile> files) {

		TransportRoute route = routeRepository.findById(request.getRouteId())
				.orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));

		TransportShift shift = route.getShift();

		// ===== SHIFT =====
		shift.setShiftName(request.getShiftName());
		shift.setStartTime(LocalTime.parse(request.getStartTime()));
		shift.setEndTime(LocalTime.parse(request.getEndTime()));
		shift.setActiveDaysMask(String.join(",", request.getActiveDays()));
		shift.setActive(true);
		shift = shiftRepository.save(shift);

		shiftHolidayRepository.deleteByShiftId(shift.getId());
		for (LocalDate holiday : request.getHolidayDates()) {
			TransportShiftHoliday sh = new TransportShiftHoliday();
			sh.setShift(shift);
			sh.setHolidayDate(holiday);
			shiftHolidayRepository.save(sh);
		}

		// ===== ROUTE =====
		route.setShift(shift);
		route.setRouteName(request.getRouteName());
		route.setRouteType(request.getRouteType());
		route.setDefaultVehicleId(request.getDefaultVehicleId());
		route.setSourceType(request.getSourceType());
		route.setRouteGeoJson(request.getRouteGeoJson());
		route.setActive(true);
		route = routeRepository.save(route);

		// ===== STOPS =====
		List<TransportRouteStop> existingStops = stopRepository.findByRouteId(route.getId());

		Map<Long, TransportRouteStop> existingMap = existingStops.stream()
				.collect(Collectors.toMap(TransportRouteStop::getId, s -> s));

		Set<Long> incomingIds = request.getStops().stream().map(StopDto::getId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		for (TransportRouteStop existing : existingStops) {
			if (!incomingIds.contains(existing.getId())) {
				stopRepository.delete(existing);
			}
		}

		List<MultipartFile> safeFiles = files != null ? files : new ArrayList<>();

		for (int i = 0; i < request.getStops().size(); i++) {

			StopDto dto = request.getStops().get(i);
			MultipartFile file = null;

			if (dto.getAnnouncementFile() == null && i < safeFiles.size()) {
				file = safeFiles.get(i);
			}

			TransportRouteStop stop;

			if (dto.getId() != null) {
				stop = existingMap.get(dto.getId());
				if (stop == null) {
					throw new RuntimeException("Stop not found: " + dto.getId());
				}
			} else {
				stop = new TransportRouteStop();
				stop.setRoute(route);
			}

			stop.setSequenceNo(dto.getSequenceNo());
			stop.setStopName(dto.getStopName());
			stop.setLatitude(dto.getLatitude());
			stop.setLongitude(dto.getLongitude());
			stop.setStopType(dto.getStopType());
			stop.setGeofenceRadius(dto.getGeofenceRadius());
			stop.setAutoDetected(Boolean.TRUE.equals(dto.getAutoDetected()));
			stop.setApproved(Boolean.TRUE.equals(dto.getApproved()));
			stop.setPassengerCount(dto.getPassengerCount() == null ? 0 : dto.getPassengerCount());
			stop.setClientStopId(dto.getClientStopId());

			if (file != null && !file.isEmpty()) {
				String fileName = storeFile(file, request.getRouteName(), dto.getStopName());
				stop.setAnnouncementFile(fileName);
			} else if (dto.getAnnouncementFile() != null) {
				stop.setAnnouncementFile(dto.getAnnouncementFile());
			}

			stopRepository.save(stop);
		}

		// ===== PASSENGERS =====
		for (PassengerAssignmentDto dto : request.getPassengers()) {

			TransportPassenger passenger;

			if (dto.getPassengerId() != null) {
				passenger = passengerRepository.findById(dto.getPassengerId()).orElseThrow(
						() -> new IllegalArgumentException("Passenger not found: " + dto.getPassengerId()));
			} else {
				passenger = new TransportPassenger();
				passenger.setCreatedAt(LocalDateTime.now());
			}

			passenger.setPassengerName(dto.getPassengerName());
			passenger.setPassengerType(dto.getPassengerType() == null ? "Student" : dto.getPassengerType());
			passenger.setGuardianName(dto.getGuardianName());
			passenger.setGuardianMobile(dto.getGuardianMobile());
			passenger.setActive(dto.getActive() == null ? true : dto.getActive());
			passenger.setUpdatedAt(LocalDateTime.now());

			passenger = passengerRepository.save(passenger);

			TransportPassengerRouteAssignment assignment = passengerRouteAssignmentRepository
					.findByPassengerIdAndRouteId(dto.getPassengerId(), route.getId())
					.orElseGet(TransportPassengerRouteAssignment::new);

			assignment.setPassenger(passenger);
			assignment.setRoute(route);

			// ===== STOPS =====
			if (dto.getPickupStopId() != null) {
				assignment.setPickupStop(stopRepository.findByClientStopId(dto.getPickupStopId())
						.orElseThrow(() -> new IllegalArgumentException("Pickup stop not found")));
			}

			if (dto.getDropStopId() != null) {
				assignment.setDropStop(stopRepository.findByClientStopId(dto.getDropStopId())
						.orElseThrow(() -> new IllegalArgumentException("Drop stop not found")));
			}

			// ===== LOGIN =====
			assignment.setAutoLoginEnabled(Boolean.TRUE.equals(dto.getAutoLoginEnabled()));
			assignment.setUsername(dto.getUsername());

			String resolvedPassword = resolvePassword(dto, route);
			if (resolvedPassword != null) {
				assignment.setTempPassword(resolvedPassword);
			}
			if (dto.getPasswordChanged() != null) {
				assignment.setPasswordChanged(dto.getPasswordChanged());
			}
			
			assignment.setActive(dto.getActive() == null ? true : dto.getActive());

			if (assignment.getId() == null) {
				assignment.setCreatedAt(LocalDateTime.now());
			}

			assignment.setUpdatedAt(LocalDateTime.now());

			passengerRouteAssignmentRepository.save(assignment);
		}

		return getRoute(route.getId());
	}

	private String resolvePassword(PassengerAssignmentDto dto, TransportRoute route) {

        // 1. AUTO LOGIN ENABLED → ALWAYS generate new password
        if (Boolean.TRUE.equals(dto.getAutoLoginEnabled())) {
            String autoPassword = String.valueOf((int) (Math.random() * 900000) + 100000);
            return passwordEncoder.encode(autoPassword);
        }

        // 2. MANUAL PASSWORD PROVIDED
        if (dto.getTempPassword() != null && !dto.getTempPassword().isBlank()) {

            if (dto.getTempPassword().startsWith("$2")) {
                return dto.getTempPassword();
            }

            return passwordEncoder.encode(dto.getTempPassword());
        }

        // 3. UPDATE CASE → fallback to existing password
        if (dto.getPassengerId() != null) {
            TransportPassengerRouteAssignment existing = passengerRouteAssignmentRepository
                    .findByPassengerIdAndRouteId(dto.getPassengerId(), route.getId()).orElse(null);

            if (existing != null) {
                return existing.getTempPassword();
            }
        }

        return null;
    }
	private String storeFile(MultipartFile file, String routeName, String stopName) {

		try {

			String routeSafe = routeName.replaceAll("[^a-zA-Z0-9]", "_");
			String stopSafe = stopName.replaceAll("[^a-zA-Z0-9]", "_");

			String originalName = file.getOriginalFilename();
			String extension = "";

			if (originalName != null && originalName.contains(".")) {
				extension = originalName.substring(originalName.lastIndexOf("."));
			}

			String cleanName = originalName != null ? originalName.replaceAll("\\s+", "_") : "file";

			String fileName = routeSafe + "_" + stopSafe + "_" + cleanName;

			Path uploadPath = Paths.get(basePath);
			Files.createDirectories(uploadPath);

			Path filePath = uploadPath.resolve(fileName);
			int counter = 1;

			while (Files.exists(filePath)) {
				fileName = routeSafe + "_" + stopSafe + "_" + counter + extension;
				filePath = uploadPath.resolve(fileName);
				counter++;
			}

			Files.write(filePath, file.getBytes());

			return fileName;

		} catch (IOException e) {
			throw new RuntimeException("Failed to store file", e);
		}
	}

	public RoutePlannerResponse getRoute(Long routeId) {
		TransportRoute route = routeRepository.findById(routeId)
				.orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

		TransportShift shift = route.getShift();
		List<TransportRouteStop> stops = stopRepository.findByRouteIdOrderBySequenceNoAsc(routeId);
		List<TransportShiftHoliday> holidays = shiftHolidayRepository.findByShiftId(shift.getId());
		List<TransportPassengerRouteAssignment> assignments = passengerRouteAssignmentRepository
				.findByRouteIdAndActiveTrue(routeId);

		RoutePlannerResponse response = new RoutePlannerResponse();
		response.setRouteId(route.getId());
		response.setShiftId(shift.getId());
		response.setRouteName(route.getRouteName());
		response.setShiftName(shift.getShiftName());
		response.setStartTime(shift.getStartTime().toString());
		response.setEndTime(shift.getEndTime().toString());
		response.setRouteType(route.getRouteType());
		response.setDefaultVehicleId(route.getDefaultVehicleId());

		if (route.getDefaultVehicle() != null) {
			response.setDefaultVehicleName(route.getDefaultVehicle().getName());
		}
		response.setSourceType(route.getSourceType());
		response.setRouteGeoJson(route.getRouteGeoJson());

		if (shift.getActiveDaysMask() != null && !shift.getActiveDaysMask().isBlank()) {
			response.setActiveDays(Arrays.asList(shift.getActiveDaysMask().split(",")));
		}

		response.setHolidayDates(
				holidays.stream().map(h -> h.getHolidayDate().toString()).collect(Collectors.toList()));

		response.setStops(stops.stream().map(s -> {
			StopDto dto = new StopDto();
			dto.setId(s.getId());
			dto.setSequenceNo(s.getSequenceNo());
			dto.setStopName(s.getStopName());
			dto.setLatitude(s.getLatitude());
			dto.setLongitude(s.getLongitude());
			dto.setStopType(s.getStopType());
			dto.setGeofenceRadius(s.getGeofenceRadius());
			dto.setAutoDetected(s.getAutoDetected());
			dto.setApproved(s.getApproved());
			dto.setPassengerCount(s.getPassengerCount());
			dto.setClientStopId(s.getClientStopId());
			dto.setAnnouncementFile(s.getAnnouncementFile());
			return dto;
		}).collect(Collectors.toList()));

		response.setPassengers(assignments.stream().map(a -> {
			PassengerAssignmentDto dto = new PassengerAssignmentDto();
			dto.setId(a.getId());
			dto.setPassengerId(a.getPassenger().getId());
			dto.setPassengerName(a.getPassenger().getPassengerName());
			dto.setPassengerType(a.getPassenger().getPassengerType());
			dto.setRouteId(route.getId());

			if (a.getPickupStop() != null) {
				dto.setPickupStopId(a.getPickupStop().getClientStopId());
				dto.setPickupStopName(a.getPickupStop().getStopName());
			}

			if (a.getDropStop() != null) {
				dto.setDropStopId(a.getDropStop().getClientStopId());
				dto.setDropStopName(a.getDropStop().getStopName());
			}

			dto.setGuardianName(a.getPassenger().getGuardianName());
			dto.setGuardianMobile(a.getPassenger().getGuardianMobile());
			dto.setAutoLoginEnabled(a.getAutoLoginEnabled());
			dto.setUsername(a.getUsername());
			dto.setTempPassword(a.getTempPassword());
			dto.setPasswordChanged(a.getPasswordChanged());
			dto.setActive(a.getActive());
			return dto;
		}).collect(Collectors.toList()));

		return response;
	}

	@Transactional
	public Page<RoutePlannerResponse> getRoutes(int page, int size, String search, String routeType) {
		if (search != null && search.isBlank()) {
			search = null;
		}
		if (routeType != null && routeType.isBlank()) {
			routeType = null;
		}

		PageRequest pageable = PageRequest.of(page, size);
		Page<TransportRoute> routes = routeRepository.searchRoutes(search, routeType, pageable);
		return routes.map(this::mapToResponse);
	}

	private RoutePlannerResponse mapToResponse(TransportRoute route) {

		TransportShift shift = route.getShift();

		List<TransportRouteStop> stops = stopRepository.findByRouteIdOrderBySequenceNoAsc(route.getId());

		List<TransportShiftHoliday> holidays = shiftHolidayRepository.findByShiftId(shift.getId());
		List<TransportPassengerRouteAssignment> assignments = passengerRouteAssignmentRepository
				.findByRouteIdAndActiveTrue(route.getId());

		RoutePlannerResponse response = new RoutePlannerResponse();

		response.setRouteId(route.getId());
		response.setShiftId(shift.getId());
		response.setRouteName(route.getRouteName());
		response.setShiftName(shift.getShiftName());
		response.setStartTime(shift.getStartTime().toString());
		response.setEndTime(shift.getEndTime().toString());
		response.setRouteType(route.getRouteType());
		response.setDefaultVehicleId(route.getDefaultVehicleId());

		if (route.getDefaultVehicle() != null) {
			response.setDefaultVehicleName(route.getDefaultVehicle().getName());
		}
		response.setSourceType(route.getSourceType());
		response.setRouteGeoJson(route.getRouteGeoJson());

		if (shift.getActiveDaysMask() != null && !shift.getActiveDaysMask().isBlank()) {
			response.setActiveDays(Arrays.asList(shift.getActiveDaysMask().split(",")));
		}

		response.setHolidayDates(
				holidays.stream().map(h -> h.getHolidayDate().toString()).collect(Collectors.toList()));

		response.setStops(stops.stream().map(s -> {
			StopDto dto = new StopDto();
			dto.setId(s.getId());
			dto.setSequenceNo(s.getSequenceNo());
			dto.setStopName(s.getStopName());
			dto.setLatitude(s.getLatitude());
			dto.setLongitude(s.getLongitude());
			dto.setStopType(s.getStopType());
			dto.setGeofenceRadius(s.getGeofenceRadius());
			dto.setAutoDetected(s.getAutoDetected());
			dto.setApproved(s.getApproved());
			dto.setPassengerCount(s.getPassengerCount());
			dto.setClientStopId(s.getClientStopId());
			dto.setAnnouncementFile(s.getAnnouncementFile());
			return dto;
		}).collect(Collectors.toList()));
		response.setPassengers(assignments.stream().map(a -> {
			PassengerAssignmentDto dto = new PassengerAssignmentDto();
			dto.setId(a.getId());
			dto.setPassengerId(a.getPassenger().getId());
			dto.setPassengerName(a.getPassenger().getPassengerName());
			dto.setPassengerType(a.getPassenger().getPassengerType());
			dto.setRouteId(route.getId());

			if (a.getPickupStop() != null) {
				dto.setPickupStopId(a.getPickupStop().getClientStopId());
				dto.setPickupStopName(a.getPickupStop().getStopName());
			}

			if (a.getDropStop() != null) {
				dto.setDropStopId(a.getDropStop().getClientStopId());
				dto.setDropStopName(a.getDropStop().getStopName());
			}
			dto.setGuardianName(a.getPassenger().getGuardianName());
			dto.setGuardianMobile(a.getPassenger().getGuardianMobile());
			dto.setAutoLoginEnabled(a.getAutoLoginEnabled());
			dto.setUsername(a.getUsername());
			dto.setTempPassword(a.getTempPassword());
			dto.setActive(a.getActive());
			return dto;
		}).collect(Collectors.toList()));
		return response;
	}
}
