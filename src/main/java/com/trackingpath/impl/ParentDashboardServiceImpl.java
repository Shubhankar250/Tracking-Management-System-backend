package com.trackingpath.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.BoardingInfo;
import com.trackingpath.dtos.DriverView;
import com.trackingpath.dtos.ParentChildSummaryDto;
import com.trackingpath.dtos.ParentChildrenResponse;
import com.trackingpath.dtos.ParentDashboardResponse;
import com.trackingpath.dtos.RouteLiveDataResponse;
import com.trackingpath.dtos.SchoolNotificationDTO;
import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.LiveData;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.entities.TripExecution;
import com.trackingpath.entities.TripPassengerExecution;
import com.trackingpath.entities.TripStopExecution;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;
import com.trackingpath.repositories.TransportRouteRepository;
import com.trackingpath.repositories.TripExecutionRepository;
import com.trackingpath.repositories.TripPassengerExecutionRepository;
import com.trackingpath.repositories.TripStopExecutionRepository;
import com.trackingpath.services.ParentDashboardService;
import com.trackingpath.util.SecurityUtils;

@Service
public class ParentDashboardServiceImpl implements ParentDashboardService {

	private final TransportPassengerRepository passengerRepository;
	private final TransportPassengerRouteAssignmentRepository assignmentRepository;
	@Autowired
	DriverRepository driverRepository;
	@Autowired
	TripExecutionRepository tripExecutionRepository;
	@Autowired
   TripPassengerExecutionRepository tripPassengerExecutionRepository;
	
	@Autowired
	TransportRouteRepository transportRouteRepository;
	@Autowired
	TripStopExecutionRepository tripStopExecutionRepository;
	public ParentDashboardServiceImpl(TransportPassengerRepository passengerRepository,
			TransportPassengerRouteAssignmentRepository assignmentRepository) {
		this.passengerRepository = passengerRepository;
		this.assignmentRepository = assignmentRepository;
	}

	@Override
	public ParentDashboardResponse getDashboard(Long passengerId) {

		TransportPassenger child = validateChildAccess(passengerId);

		TransportPassengerRouteAssignment assignment = assignmentRepository.findFullAssignment(passengerId)
				.orElseThrow(() -> new IllegalArgumentException("Route not assigned"));

		String routeName = assignment.getRoute().getRouteName();

		String pickupStop = assignment.getPickupStop() != null ? assignment.getPickupStop().getStopName() : null;

		String vehicleNumber = assignment.getRoute().getDefaultVehicle() != null
				? assignment.getRoute().getDefaultVehicle().getName() // or getUniqueId()
				: null;

		return ParentDashboardResponse.builder().passengerId(child.getId()).passengerName(child.getPassengerName())
				.className(child.getClassName()).sectionName(child.getSectionName())

				.routeName(routeName).pickupStop(pickupStop).vehicleNumber(vehicleNumber).tripStatus("IN_PROGRESS")
				.boardingStatus("BOARDED").attendanceStatus("PRESENT").boardedTime("2026-04-17T07:12:00")
				.schoolEta("2026-04-17T07:54:00").distanceKm(4.2).notificationCount(3)

				.build();
	}

	@Override
	public ParentChildrenResponse getChildren() {

	    String guardianMobile = getCurrentGuardianMobile();

	    // ================== ASSIGNMENTS ==================
	    List<TransportPassengerRouteAssignment> assignments =
	            assignmentRepository.findAllByGuardianMobile(guardianMobile);

	    List<Long> routeIds = assignments.stream()
	            .map(a -> a.getRoute().getId())
	            .distinct()
	            .toList();

	    List<Long> passengerIds = assignments.stream()
	            .map(a -> a.getPassenger().getId())
	            .toList();

	    // ================== BOARDING DATA (BULK) ==================
	    List<TripPassengerExecution> executions =
	            tripPassengerExecutionRepository.findTodayData(passengerIds, routeIds);

	    Map<Long, BoardingInfo> boardingMap = executions.stream()
	            .collect(Collectors.toMap(
	                    TripPassengerExecution::getPassengerId,
	                    e -> new BoardingInfo(
	                            e.getBoardingStatus(),
	                            e.getBoardTime()
	                    )
	            ));

	    // ================== TRIP EXECUTION SNAPSHOT (BULK) ==================
	    List<TripExecution> trips =
	            tripExecutionRepository.findTodayTripsByRouteIds(routeIds);

	    Map<Long, TripExecution> tripMap = trips.stream()
	            .collect(Collectors.toMap(
	                    TripExecution::getRouteId,
	                    t -> t
	            ));

	    // ================== FINAL RESPONSE ==================
	    List<ParentChildSummaryDto> children = assignments.stream().map(a -> {

	        TransportPassenger p = a.getPassenger();
	        Long routeId = (a.getRoute() != null) ? a.getRoute().getId() : null;

	        TripExecution trip = (routeId != null) ? tripMap.get(routeId) : null;

	        // ================== TRIP STATUS ==================
	        String tripStatus = (trip != null)
	                ? trip.getTripStatus()
	                : "NOT_STARTED";

	        // ================== ROUTE NAME (SNAPSHOT FIRST) ==================
	        String routeName = (trip != null && trip.getRouteNameSnapshot() != null)
	                ? trip.getRouteNameSnapshot()
	                : (a.getRoute() != null ? a.getRoute().getRouteName() : null);

	        // ================== SHIFT NAME (SNAPSHOT FIRST) ==================
	        String shiftName = (trip != null && trip.getShiftNameSnapshot() != null)
	                ? trip.getShiftNameSnapshot()
	                : (a.getRoute() != null && a.getRoute().getShift() != null
	                    ? a.getRoute().getShift().getShiftName()
	                    : null);

	        // ================== BOARDING INFO ==================
	        BoardingInfo info = boardingMap.get(p.getId());

	        String boardingStatus = (info != null)
	                ? info.getBoardingStatus()
	                : "NOT_BOARDED";

	        LocalDateTime boardTime = (info != null)
	                ? info.getBoardTime()
	                : null;

	        // ================== DRIVER ==================
	        Long vehicleId = (a.getRoute() != null)
	                ? a.getRoute().getDefaultVehicleId()
	                : null;

	        String driverName = null;

	        if (vehicleId != null) {
	            List<DriverView> drivers = driverRepository.findByDevice_Id(vehicleId);

	            if (drivers != null && !drivers.isEmpty()) {
	                driverName = drivers.get(0).getName();
	            }
	        }

	        // ================== DTO ==================
	        return ParentChildSummaryDto.builder()
	                .passengerId(p.getId())
	                .passengerName(p.getPassengerName())
	                .className(p.getClassName())

	                .routeName(routeName)
	                .shiftName(shiftName)

	                .pickupStop(a.getPickupStop() != null
	                        ? a.getPickupStop().getStopName()
	                        : null)

	                .vehicleName(
	                        a.getRoute() != null && a.getRoute().getDefaultVehicle() != null
	                                ? a.getRoute().getDefaultVehicle().getName()
	                                : null
	                )

	                .driverName(driverName)

	                .tripStatus(tripStatus)
	                .boardingStatus(boardingStatus)
	                .boardTime(boardTime)

	                .build();

	    }).toList();

	    return ParentChildrenResponse.builder()
	            .parentId(0L)
	            .children(children)
	            .build();
	}

	private ParentChildSummaryDto toChildSummary(TransportPassenger p) {
		return ParentChildSummaryDto.builder().passengerId(p.getId()).passengerName(p.getPassengerName())
				.className(p.getClassName()).routeName("TODO").pickupStop("TODO").tripStatus("NOT_STARTED")
				.boardingStatus("PENDING").build();
	}

	private TransportPassenger validateChildAccess(Long passengerId) {

		String guardianMobile = getCurrentGuardianMobile();

		TransportPassenger child = passengerRepository.findById(passengerId)
				.orElseThrow(() -> new IllegalArgumentException("Child not found"));

		if (!guardianMobile.equals(child.getGuardianMobile())) {
			throw new SecurityException("You are not allowed to access this child");
		}

		return child;
	}

	private String getCurrentGuardianMobile() {
		Long loginPassengerId = SecurityUtils.getCurrentUser().getPassengerLoginId();

		TransportPassenger passenger = passengerRepository.findById(loginPassengerId).orElseThrow();

		return passenger.getGuardianMobile();
	}

	@Override
	public RouteLiveDataResponse getLiveData() {

	    String guardianMobile = getCurrentGuardianMobile();

	    // ================= ASSIGNMENTS =================
	    List<TransportPassengerRouteAssignment> assignments =
	            assignmentRepository.findAllByGuardianMobile(guardianMobile);

	    if (assignments.isEmpty()) {
	        throw new RuntimeException("No route assigned");
	    }

	    // Extract IDs
	    List<Long> routeIds = assignments.stream()
	            .map(a -> a.getRoute().getId())
	            .distinct()
	            .toList();

	    List<Long> passengerIds = assignments.stream()
	            .map(a -> a.getPassenger().getId())
	            .toList();

	    TransportPassengerRouteAssignment assignment = assignments.get(0);
	    Long routeId = assignment.getRoute().getId();

	    // ================= LIVE DATA =================
	    LiveData live = transportRouteRepository
	            .findLiveDataByRouteId(routeId)
	            .stream()
	            .findFirst()
	            .orElse(null);

	    if (live == null) {
	        throw new RuntimeException("No live data found");
	    }

	    // ================= NOTIFICATION (USE EXISTING QUERY) =================
	    List<TripPassengerExecution> executions =
	            tripPassengerExecutionRepository.findTodayData(passengerIds, routeIds);
	    
	    List<Long> tripExecutionIds = executions.stream()
	            .map(e -> e.getTripExecution().getId())
	            .distinct()
	            .toList();
	    List<TripStopExecution> stops =
	            tripStopExecutionRepository.findAllStopsByTripExecutionIds(tripExecutionIds);
	    
	    Map<Long, TripStopExecution> stopMap = stops.stream()
	            .collect(Collectors.toMap(
	                    TripStopExecution::getRouteStopId,
	                    s -> s,
	                    (a, b) -> a
	            ));
	    List<TripStopExecution> sortedStops = stopMap.values().stream()
	            .sorted(Comparator.comparing(TripStopExecution::getStopSequence))
	            .toList();
	    TripStopExecution currentStop = null;

	    if (!executions.isEmpty()) {
	        currentStop = executions.get(0).getTripStopExecution();
	    }
	    String nextLandmark = "Trip Completed"; // default

	    if (currentStop != null) {

	        Integer currentSeq = currentStop.getStopSequence();

	        for (TripStopExecution stop : sortedStops) {
	            if (stop.getStopSequence() > currentSeq) {
	                nextLandmark = stop.getStopNameSnapshot();
	                break;
	            }
	        }
	    }
	    List<SchoolNotificationDTO> notifications = executions.stream()

	            // 🔥 optional filter (only boarded)
	            .filter(e -> "BOARDED".equalsIgnoreCase(e.getBoardingStatus()))

	            .map(e -> {
	                SchoolNotificationDTO dto = new SchoolNotificationDTO();

	                dto.setPassengername(e.getPassengerNameSnapshot());
	                dto.setBoardedtime(e.getBoardTime());

	                if (e.getTripStopExecution() != null) {
	                    dto.setEta(e.getTripStopExecution().getEtaTime());
	                }

	                return dto;
	            })
	            .toList();

	    // ================= FINAL RESPONSE =================
	    return buildResponse(live, assignment, notifications, executions, stopMap,nextLandmark);
	    }
	
	
	private RouteLiveDataResponse buildResponse(
	        LiveData live,
	        TransportPassengerRouteAssignment assignment,
	        List<SchoolNotificationDTO> notifications,
	        List<TripPassengerExecution> executions,
	        Map<Long, TripStopExecution> stopMap,
	        String nextLandmark) {

	    RouteLiveDataResponse res = new RouteLiveDataResponse();

	    res.setLatitude(live.getLatitude());
	    res.setLongitude(live.getLongitude());
	    res.setSpeed(live.getSpeed() != null ? live.getSpeed().longValue() : 0);
	    res.setAddress(live.getAddress());

	    res.setVehiclename(
	            assignment.getRoute().getDefaultVehicle() != null
	                    ? assignment.getRoute().getDefaultVehicle().getName()
	                    : null
	    );

	    res.setNextlanmark(nextLandmark);
	    res.setSchooleta(LocalDateTime.now().plusMinutes(15));

	    // 🔥 IMPORTANT
	    res.setNotification(notifications);
	    if (!executions.isEmpty()) {

	        TripPassengerExecution tpe = executions.get(0);

	        // Pickup
	        TripStopExecution pickupStop = stopMap.get(tpe.getPickupStopId());
	        if (pickupStop != null) {
	            res.setPickupLatitude(pickupStop.getLatitude());
	            res.setPickupLongitude(pickupStop.getLongitude());
	            res.setPickupStopName(tpe.getPickupStopNameSnapshot());
	        }

	        // Drop
	        TripStopExecution dropStop = stopMap.get(tpe.getDropStopId());
	        if (dropStop != null) {
	            res.setDropLatitude(dropStop.getLatitude());
	            res.setDropLongitude(dropStop.getLongitude());
	            res.setDropStopName(tpe.getDropStopNameSnapshot());
	        }
	    }
	    return res;
	}
	
	
}