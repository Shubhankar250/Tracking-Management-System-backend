package com.trackingpath.impl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.BreakdownDetailsDto;
import com.trackingpath.dtos.DriverDashboardResponse;
import com.trackingpath.dtos.DriverLiveTripDto;
import com.trackingpath.dtos.LiveStopDto;
import com.trackingpath.dtos.PassengerListDto;
import com.trackingpath.dtos.StopListDto;
import com.trackingpath.dtos.VehicleEventRequest;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.StaffDetails;
import com.trackingpath.entities.TransportRoute;
import com.trackingpath.entities.TripExecution;
import com.trackingpath.entities.TripPassengerExecution;
import com.trackingpath.entities.TripStopExecution;
import com.trackingpath.entities.TripVehicleEvent;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.StaffDetailsRepository;
import com.trackingpath.repositories.TransportRouteRepository;
import com.trackingpath.repositories.TripExecutionRepository;
import com.trackingpath.repositories.TripPassengerExecutionRepository;
import com.trackingpath.repositories.TripStopExecutionRepository;
import com.trackingpath.repositories.TripVehicleEventRepository;
import com.trackingpath.services.DriverDashboardService;

@Service
public class DriverDashboardServiceImpl implements DriverDashboardService {

    private final DriverRepository driverRepository;
    private final TripExecutionRepository tripExecutionRepository;
    private final DeviceRepository deviceRepository;
    private final TripStopExecutionRepository tripStopExecutionRepository;
    private final  TransportRouteRepository transportRouteRepository;
    private final TripPassengerExecutionRepository tripPassengerExecutionRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final TripVehicleEventRepository tripVehicleEventRepository;

    public DriverDashboardServiceImpl(DriverRepository driverRepository,
                                        TripExecutionRepository tripExecutionRepository,
                                        DeviceRepository deviceRepository,                                     
                                        TripStopExecutionRepository tripStopExecutionRepository,
                                        TransportRouteRepository transportRouteRepository,
                                        TripPassengerExecutionRepository tripPassengerExecutionRepository,
                                        StaffDetailsRepository staffDetailsRepository,TripVehicleEventRepository tripVehicleEventRepository) {
        this.driverRepository = driverRepository;
        this.tripExecutionRepository = tripExecutionRepository;
        this.deviceRepository = deviceRepository;
        this.tripStopExecutionRepository = tripStopExecutionRepository;
        this.transportRouteRepository=transportRouteRepository;
        this.tripPassengerExecutionRepository=tripPassengerExecutionRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.tripVehicleEventRepository=tripVehicleEventRepository;
        }

    @Override
    public DriverDashboardResponse getDashboard(Long driverId) {

        // 1. Driver Fetch
        Driveres driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        Long deviceId = null;

        if (driver.getCurrentDevice() != null) {
            deviceId = driver.getCurrentDevice().getId();
        } else if (driver.getDevice() != null) {
            deviceId = driver.getDevice().getId();
        }

        // 3. Fetch device separately
        String vehicleName = null;

        if (deviceId != null) {
            DeviceEntity device = deviceRepository.findById(deviceId).orElse(null);
            if (device != null) {
                vehicleName = device.getName();
            }
        }
             
    System.out.println("vehicleName"+vehicleName);
        // 3. TripExecution (latest or active trip)
        Optional<TripExecution> tripOpt =
                tripExecutionRepository.findTopByDriverIdOrderByCreatedAtDesc(driverId);

        TripExecution trip = tripOpt.orElse(null);

        String routeName = null;
        String tripTime = null;
        Double progress = 0.0;

        String totalPassenger = "0";
        String present = "0";
        String absent = "0";
        String boarded = "0";
        long totalStops=0;
        long coveredStops=0;
        String depotDeparture = null;
        String upcoming_stop = null;
        String nextStop = null;
        String lastStop = null;
        String shiftName = null;
        LocalDateTime starttime = null;
        LocalDateTime expectedTime = null;
        String starttimeStr = null;
        String  expectedTimeStr = null;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        if (trip != null) {

            // 4. Route Name
            //Optional<TransportRoute> routeOpt =
                   // transportRouteRepository.findById(trip.getRouteId());

          //  if (routeOpt.isPresent()) {
               // routeName = routeOpt.get().getRouteName();
          //  }
                routeName = trip.getRouteNameSnapshot();
                shiftName = trip.getShiftNameSnapshot();
                
                starttime=  toIst(trip.getActualStartTime());
                expectedTime = toIst(trip.getPlannedEndTime());
                if (starttime != null) {
                	starttimeStr = starttime.format(timeFormatter);
                }

                if (expectedTime != null) {
                	expectedTimeStr = expectedTime.format(timeFormatter);
                }
            // 5. Trip Time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                if (trip.getActualStartTime() != null && trip.getActualEndTime() != null) {

                    String startTime = trip.getActualStartTime()
                            .atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                            .toLocalTime()
                            .format(formatter);

                    String endTime = trip.getActualEndTime()
                            .atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                            .toLocalTime()
                            .format(formatter);

                    tripTime = startTime + " - " + endTime;
                }
                if (trip.getActualStartTime() != null) {
                    depotDeparture = trip.getActualStartTime()
                            .atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm a"));
                }
               

                List<TripStopExecution> stops =
                        tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

                if (stops != null && !stops.isEmpty()) {

                    // ✅ Last Stop
                    lastStop = stops.get(stops.size() - 1).getStopNameSnapshot();

                    // ✅ Upcoming (sequence 2 → index 1)
                    if (stops.size() > 1) {
                        upcoming_stop = stops.get(1).getStopNameSnapshot();
                    }

                    // ✅ Next (sequence 3 → index 2)
                    if (stops.size() > 2) {
                        nextStop = stops.get(2).getStopNameSnapshot();
                    }
                }
            // 6. Passenger Data
            totalPassenger = String.valueOf(trip.getTotalPassengersAssigned());
            present = String.valueOf(trip.getTotalPresent());
            absent = String.valueOf(trip.getTotalAbsent());
            boarded = String.valueOf(trip.getTotalBoarded());
            totalStops=trip.getTotalStops();
            coveredStops=trip.getCoveredStops();
            // 7. Progress
            if (trip.getStopCoveragePercent() != null) {
                progress = trip.getStopCoveragePercent().doubleValue();
            }
        }

        // 8. Build Response
        return DriverDashboardResponse.builder()
                .driverId(driver.getId())
                .driverName(driver.getName())
                .active(driver.getActive())
                .driverNumber(driver.getPhone())
                .helperName("Ramesh Kumar")
                .schoolName("Delhi Public School")
                .vehicleName(vehicleName)
                .routeName(routeName)
                .upcomingStop(upcoming_stop)
                .nextStop(nextStop)
                .depotDeparture(depotDeparture)
                .totalPassengerAssign(totalPassenger)
                .presentPassenger(present)
                .absentPassenger(absent)
                .boardedCount(boarded)
                .tripTime(tripTime)
                .tripProgress(progress)
                .totalStops(totalStops)
                .coveredStops(coveredStops)
                .lastStop(lastStop)
                .startTime(starttimeStr)
                .expectedTime(expectedTimeStr)
                .shiftName(shiftName)
                .build();
                        
    }
    @Override
    public List<StopListDto> getStopList(Long driverId) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TripStopExecution> stops =
                tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

        // ✅ Driver timezone (for now fixed)
        ZoneId zone = ZoneId.of("Asia/Kolkata");

        // ✅ Current time in IST
        LocalDateTime now = LocalDateTime.now(zone);

        return stops.stream().map(stop -> {

            String eta = null;
            long boarded = 0;

            // ✅ COMPLETED STOP
            if ("COMPLETED".equalsIgnoreCase(stop.getStopStatus())) {

                boarded = stop.getBoardedCount() != null ? stop.getBoardedCount() : 0;
                eta = null;

            } else {

                if (stop.getEtaTime() != null) {

                    // ✅ Convert ETA (stored in UTC) → IST
                    LocalDateTime etaIst = stop.getEtaTime()
                            .atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(zone)
                            .toLocalDateTime();

                    long minutes = java.time.Duration.between(now, etaIst).toMinutes();

                    if (minutes > 0) {
                        eta = minutes + " min";
                    } else {
                        eta = "Arriving";
                    }
                }
            }

            return StopListDto.builder()
                    .sequence(stop.getStopSequence())
                    .stopName(stop.getStopNameSnapshot())
                    .etaTime(eta)
                    .status(stop.getStopStatus())
                    .boarded(boarded)
                    .build();

        }).toList();
    }
    @Override
    public DriverLiveTripDto getLiveTrip(Long driverId) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TripStopExecution> stops =
                tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

        TransportRoute route = transportRouteRepository.findById(trip.getRouteId())
                .orElse(null);
       
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDateTime now = LocalDateTime.now(zone);

        String currentStop = null;
        String nextStop = null;
        String nextStopEta = null;
        int currentStopBoarded = 0;
        int currentStopExpected = 0;
        int currentStopAbsent = 0;      
        String delay = null;
        LocalDateTime scheduledArrivalTime = null;
        LocalDateTime actualArrivalTime = null;
        String scheduledTimeStr = null;
        String actualTimeStr = null;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String fileName = null;   
        String vehicleName = null;
     
        if (trip.getActualVehicleId() != null) {
            Optional<DeviceEntity> deviceOpt = deviceRepository.findById(trip.getActualVehicleId());
            if (deviceOpt.isPresent()) {
                vehicleName = deviceOpt.get().getName(); // change field if different
            }
        }
       
        
        List<LiveStopDto> stopDtos = new ArrayList<>();

        boolean currentAssigned = false;
               
        for (int i = 0; i < stops.size(); i++) {

            TripStopExecution stop = stops.get(i);

            String status;
            String eta = null;

            if (Boolean.TRUE.equals(stop.getIsCovered())) {

                status = "COMPLETED";

            } else if (!currentAssigned) {

                // ✅ THIS IS THE FIRST NON-COMPLETED AFTER COMPLETED
                status = "CURRENT";
                currentAssigned = true;

                currentStop = stop.getStopNameSnapshot();

                // 👉 CURRENT STOP DATA
                currentStopBoarded = stop.getBoardedCount() != null ? stop.getBoardedCount() : 0;
                currentStopExpected = stop.getAssignedPassengerCount() != null ? stop.getAssignedPassengerCount() : 0;
                currentStopAbsent = stop.getAbsentCount() != null ? stop.getAbsentCount() : 0;
                scheduledArrivalTime = toIst(stop.getPlannedArrivalTime());
                actualArrivalTime = toIst(stop.getActualArrivalTime());

                if (scheduledArrivalTime != null) {
                    scheduledTimeStr = scheduledArrivalTime.format(timeFormatter);
                }

                if (actualArrivalTime != null) {
                    actualTimeStr = actualArrivalTime.format(timeFormatter);
                }
                                                     
                // 👉 NEXT STOP
                if (i + 1 < stops.size()) {

                    TripStopExecution next = stops.get(i + 1);
                    nextStop = next.getStopNameSnapshot();

                    if (next.getEtaTime() != null) {
                        LocalDateTime etaIst = next.getEtaTime()
                                .atZone(ZoneId.of("UTC"))
                                .withZoneSameInstant(zone)
                                .toLocalDateTime();

                        long min = Duration.between(now, etaIst).toMinutes();
                        if (min <= 0) min = 1;

                        nextStopEta = min + " min";
                    }
                    // 🔥 ✅ NEXT STOP DELAY CALCULATION
                    if (next.getPlannedArrivalTime() != null && next.getEtaTime() != null) {

                        LocalDateTime planned = next.getPlannedArrivalTime();

                        LocalDateTime etaIst = next.getEtaTime()
                                .atZone(ZoneId.of("UTC"))
                                .withZoneSameInstant(zone)
                                .toLocalDateTime();

                        long delayMin = Duration.between(planned, etaIst).toMinutes();

                        if (delayMin > 0) {
                            delay = delayMin + " min late";
                        } else if (delayMin < 0) {
                            delay = Math.abs(delayMin) + " min early";
                        } else {
                            delay = "On time";
                        }
                    }
                }

            } else {
                status = "UPCOMING";
            }

            // 👉 ETA for non-completed
            if (!"COMPLETED".equals(status) && stop.getEtaTime() != null) {

                LocalDateTime etaIst = stop.getEtaTime()
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(zone)
                        .toLocalDateTime();

                long min = Duration.between(now, etaIst).toMinutes();
                if (min <= 0) min = 1;

                eta = min + " min";
            } 
            fileName = stop.getAnnouncementFile();
           
            stopDtos.add(
                    LiveStopDto.builder()
                            .sequence(stop.getStopSequence())
                            .stopName(stop.getStopNameSnapshot())
                            .latitude(stop.getLatitude())
                            .longitude(stop.getLongitude())
                            .status(stop.getStopStatus())
                            .boarded(stop.getBoardedCount())
                            .announcementFile(fileName)
                            .eta(eta)
                            .build()
            );
        }

          

        

        return DriverLiveTripDto.builder()
                .routeName(trip.getRouteNameSnapshot())
                .routeGeoJson(route != null ? route.getRouteGeoJson() : null)
                .currentStop(currentStop)
                .nextStop(nextStop)
                .nextStopEta(nextStopEta)
                .delay(delay)
                .vehicleId(trip.getActualVehicleId())
                .vehicleName(vehicleName)
                .totalStudents(currentStopExpected)
                .boarded(currentStopBoarded)
                .pending(currentStopAbsent)
                .stops(stopDtos)
                .actualArrivalTime(actualTimeStr)
                .scheduleArrivalTime(scheduledTimeStr)
                .build();
    }
    private LocalDateTime toIst(LocalDateTime utcTime) {
        if (utcTime == null) return null;

        return utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .toLocalDateTime();
    }
    
    @Override
    public List<PassengerListDto> getPassengerList(Long driverId, String pickupStop, String attendanceStatus) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TripPassengerExecution> passengers;

        boolean isPickup = "PICKUP".equalsIgnoreCase(trip.getRouteType());

        // ✅ Dynamic filtering based on routeType
        if (pickupStop != null && attendanceStatus != null) {

            if (isPickup) {
                passengers = tripPassengerExecutionRepository
                        .findByTripIdPickupAndStatusWithStop(
                                trip.getId(), pickupStop, attendanceStatus);
            } else {
                passengers = tripPassengerExecutionRepository
                        .findByTripIdDropAndStatusWithStop(
                                trip.getId(), pickupStop, attendanceStatus);
            }

        } else if (pickupStop != null) {

            if (isPickup) {
                passengers = tripPassengerExecutionRepository
                        .findByTripIdAndPickupStopWithStop(
                                trip.getId(), pickupStop);
            } else {
                passengers = tripPassengerExecutionRepository
                        .findByTripIdAndDropStopWithStop(
                                trip.getId(), pickupStop);
            }

        } else if (attendanceStatus != null) {

            passengers = tripPassengerExecutionRepository
                    .findByTripIdAndStatusWithStop(
                            trip.getId(), attendanceStatus);

        } else {

            passengers = tripPassengerExecutionRepository
                    .findByTripExecutionIdWithStop(trip.getId());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        return passengers.stream().map(p -> {

            String expectedTime = null;

         

if (p.getTripStopExecution() != null &&
    p.getTripStopExecution().getPlannedArrivalTime() != null) {

    expectedTime = toIst(
            p.getTripStopExecution().getPlannedArrivalTime()
    ).format(formatter);
}
                   
            return PassengerListDto.builder()
                    .passengerName(p.getPassengerNameSnapshot())
                    .passengerId(p.getPassengerId())
                    .pickupStop(
                            isPickup 
                            ? p.getPickupStopNameSnapshot() 
                            : p.getDropStopNameSnapshot()
                    )
                    .guardianName(p.getGuardianNameSnapshot())
                    .guardianNumber(p.getGuardianMobileSnapshot())
                    .className(p.getClass_name())
                    .rollNumber(p.getRole_number())
                    .expectedTime(expectedTime)
                    .attendanceStatus(p.getAttendanceStatus())
                   .boardingStatus(p.getBoardingStatus())
                   .deboardingStatus(p.getDeboardingStatus())
                   .guardianInformed(p.getGuardianInformed())
                   .guardianVerified(p.getGuardianVerified())
                    .build();

        }).toList();
    }
    @Override
    public List<StaffDetails> getAllStaffs() {
        return staffDetailsRepository.findAll();
    }
    @Override
    public void markAllBoarded(Long driverId, String pickupStop, String attendanceStatus) {

        List<TripPassengerExecution> passengers = getFilteredPassengers(driverId, pickupStop, attendanceStatus);

        for (TripPassengerExecution p : passengers) {
            p.setBoardingStatus("BOARDED");
            p.setBoardTime(LocalDateTime.now());
            p.setBoardSource("MANUAL");
        }

        tripPassengerExecutionRepository.saveAll(passengers);
    }
    @Override
    public void markAllDeboarded(Long driverId, String pickupStop, String attendanceStatus) {

        List<TripPassengerExecution> passengers =
                getFilteredPassengers(driverId, pickupStop, attendanceStatus);

        for (TripPassengerExecution p : passengers) {
            p.setDeboardingStatus("DEBOARDED");
            p.setDeboardTime(LocalDateTime.now());
            p.setDeboardSource("MANUAL");
        }

        tripPassengerExecutionRepository.saveAll(passengers);
    }
    @Override
    public void markAllAbsent(Long driverId, String pickupStop, String attendanceStatus) {

        List<TripPassengerExecution> passengers =
                getFilteredPassengers(driverId, pickupStop, attendanceStatus);

        for (TripPassengerExecution p : passengers) {
            p.setAttendanceStatus("ABSENT");
            p.setAttendanceMarkTime(LocalDateTime.now());
            p.setAttendanceMarkSource("MANUAL");
        }

        tripPassengerExecutionRepository.saveAll(passengers);
    }
    private List<TripPassengerExecution> getFilteredPassengers(
            Long driverId, String pickupStop, String attendanceStatus) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        boolean isPickup = "PICKUP".equalsIgnoreCase(trip.getRouteType());

        if (pickupStop != null && attendanceStatus != null) {

            return isPickup
                    ? tripPassengerExecutionRepository.findByTripIdPickupAndStatusWithStop(
                            trip.getId(), pickupStop, attendanceStatus)
                    : tripPassengerExecutionRepository.findByTripIdDropAndStatusWithStop(
                            trip.getId(), pickupStop, attendanceStatus);

        } else if (pickupStop != null) {

            return isPickup
                    ? tripPassengerExecutionRepository.findByTripIdAndPickupStopWithStop(
                            trip.getId(), pickupStop)
                    : tripPassengerExecutionRepository.findByTripIdAndDropStopWithStop(
                            trip.getId(), pickupStop);

        } else if (attendanceStatus != null) {

            return tripPassengerExecutionRepository.findByTripIdAndStatusWithStop(
                    trip.getId(), attendanceStatus);

        } else {

            return tripPassengerExecutionRepository.findByTripExecutionIdWithStop(trip.getId());
        }
    }
    @Override
    public void markPassengerBoarded(Long id) {

        TripPassengerExecution p = tripPassengerExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        // ✅ Update fields
        p.setAttendanceStatus("PRESENT");
        p.setBoardingStatus("BOARDED");

        p.setAttendanceMarkTime(LocalDateTime.now());
        p.setAttendanceMarkSource("MANUAL");

        p.setBoardTime(LocalDateTime.now());
        p.setBoardSource("MANUAL");
        p.setGuardianInformed(true);

        tripPassengerExecutionRepository.save(p);
    }
    @Override
    public void markPassengerAbsent(Long id) {

        TripPassengerExecution p = tripPassengerExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        p.setAttendanceStatus("ABSENT");

        p.setAttendanceMarkTime(LocalDateTime.now());
        p.setAttendanceMarkSource("MANUAL");

        tripPassengerExecutionRepository.save(p);
    }
    @Override
    public void reportBreakdown(VehicleEventRequest request) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        TripVehicleEvent event = new TripVehicleEvent();

        event.setTripExecutionId(trip.getId());
        event.setEventType("BREAKDOWN");
        event.setOldVehicleId(trip.getActualVehicleId());
        event.setEventTime(LocalDateTime.now());
        event.setReason(request.getReason());
        event.setRemarks(request.getRemarks());

        tripVehicleEventRepository.save(event);

        // ✅ Update trip status
        trip.setTripStatus("DELAYED");
        trip.setStatusReason("Vehicle Breakdown");
        tripExecutionRepository.save(trip);
    }
    @Override
    public void replaceVehicle(VehicleEventRequest request) {

        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Long oldVehicle = trip.getActualVehicleId();

        // ✅ 1. Fetch existing event (IMPORTANT for UPDATE)
        TripVehicleEvent event = tripVehicleEventRepository
                .findTopByTripExecutionIdOrderByCreatedAtDesc(trip.getId())
                .orElseGet(TripVehicleEvent::new);

        // ✅ 2. Set values
        event.setTripExecutionId(trip.getId());
        event.setEventType("REPLACED");
        event.setOldVehicleId(oldVehicle);
        event.setNewVehicleId(request.getNewVehicleId());
        event.setEventTime(LocalDateTime.now());           
        event.setDriverId(request.getDriverId());
        event.setUpdatedAt(LocalDateTime.now());
        //event.setUserId(request.getUserId());

        // ⚠️ If new object, createdAt set via @PrePersist
        // ⚠️ If existing object, updatedAt set via @PreUpdate

        tripVehicleEventRepository.save(event); // ✅ UPDATE होगा अगर ID present है

        // ✅ 3. Update trip_execution
        trip.setActualVehicleId(request.getNewVehicleId());
        trip.setVehicleReplaced(true);
        trip.setReplacementCount(
                trip.getReplacementCount() == null ? 1 : trip.getReplacementCount() + 1
        );
        trip.setTripStatus("IN_PROGRESS");

        tripExecutionRepository.save(trip);

        // ✅ 4. Update upcoming stops
        List<TripStopExecution> stops =
                tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

        for (TripStopExecution stop : stops) {
            if (!Boolean.TRUE.equals(stop.getIsCovered())) {
                stop.setVehicleIdAtStop(request.getNewVehicleId());
            }
        }

        tripStopExecutionRepository.saveAll(stops);
    }
    @Override
    public BreakdownDetailsDto getBreakdownDetails(Long driverId) {

        // 🔴 1. Get latest trip
        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Long tripExecutionId = trip.getId();

        // 🔴 2. Get event (single row system)
        TripVehicleEvent event = tripVehicleEventRepository
                .findByTripExecutionIdOrderByEventTimeAsc(tripExecutionId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 🔴 3. Find breakdown stop (use createdAt)
        List<TripStopExecution> stops =
                tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(tripExecutionId);

        TripStopExecution breakdownStop = null;

        for (TripStopExecution stop : stops) {
            if (stop.getActualArrivalTime() != null &&
                    !stop.getActualArrivalTime().isAfter(event.getCreatedAt())) {
                breakdownStop = stop;
            }
        }

        if (breakdownStop == null && !stops.isEmpty()) {
            breakdownStop = stops.get(0);
        }

        // 🔴 4. Count boarded students
        int boardedCount = (int) tripPassengerExecutionRepository
                .findByTripExecutionIdWithStop(tripExecutionId)
                .stream()
                .filter(p -> "BOARDED".equalsIgnoreCase(p.getBoardingStatus()))
                .count();

        // 🔴 5. Breakdown time (created_at)
        String breakdownTime = event.getCreatedAt()
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("hh:mm a"));

        // 🔴 6. Estimated support
        String estimatedSupport = "20 min";

        // 🟢 7. Replacement data (same row)
        String newVehicleName = null;
        String driverName = null;
        String replacementEta = null;

        // vehicle
        if (event.getNewVehicleId() != null) {
            DeviceEntity device = deviceRepository
                    .findById(event.getNewVehicleId())
                    .orElse(null);

            if (device != null) {
                newVehicleName = device.getName();
            }
        }

        // driver
        Driveres driver = driverRepository.findById(driverId).orElse(null);
        if (driver != null) {
            driverName = driver.getName();
        }

        // ETA (only if updated_at present)
        if (event.getUpdatedAt() != null) {
            long minutes = Duration.between(
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            ).toMinutes();

            replacementEta = minutes + " min";
        }

        return BreakdownDetailsDto.builder()
                .routeName(trip.getRouteNameSnapshot())
                .breakdownStopName(breakdownStop != null ? breakdownStop.getStopNameSnapshot() : null)
                .breakdownTime(breakdownTime)
                .boardedStudentCount(boardedCount)
                .estimatedSupportTimeMinutes(estimatedSupport)
                .replacementDeviceName(newVehicleName)
                .replacementDriverName(driverName)
                .etaMinutes(replacementEta)
                .build();
    }
    @Override
    public List<TripVehicleEvent> getVehicleEventsByDriver(Long driverId) {

        // 🔴 Step 1: driver ka latest trip nikalo
        TripExecution trip = tripExecutionRepository
                .findTopByDriverIdOrderByCreatedAtDesc(driverId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 🔴 Step 2: us trip ke events nikalo
        return tripVehicleEventRepository
                .findByTripExecutionIdOrderByEventTimeAsc(trip.getId());
    }
}