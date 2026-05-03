package com.trackingpath.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.trackingpath.dtos.*;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.TransportRoute;
import com.trackingpath.entities.TripExecution;
import com.trackingpath.entities.TripPassengerExecution;
import com.trackingpath.entities.TripStopExecution;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.ParentChildMapRepository;
import com.trackingpath.repositories.TransportRouteRepository;
import com.trackingpath.repositories.TripPassengerExecutionRepository;
import com.trackingpath.repositories.TripStopExecutionRepository;
import com.trackingpath.services.ParentTripService;
import com.trackingpath.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class ParentTripServiceImpl implements ParentTripService {

    private final ParentChildMapRepository parentChildMapRepository;
    private final TripPassengerExecutionRepository tripPassengerExecutionRepository;
    private final TripStopExecutionRepository tripStopExecutionRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final DeviceRepository deviceRepository;

    public ParentTripServiceImpl(ParentChildMapRepository parentChildMapRepository,
    		TripPassengerExecutionRepository tripPassengerExecutionRepository,
    		TripStopExecutionRepository tripStopExecutionRepository,
    		TransportRouteRepository transportRouteRepository,DeviceRepository deviceRepository) {
        this.parentChildMapRepository = parentChildMapRepository;
        this.tripPassengerExecutionRepository=tripPassengerExecutionRepository;
        this.tripStopExecutionRepository = tripStopExecutionRepository;
        this.transportRouteRepository = transportRouteRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    @Transactional
    public LiveTrackingResponse getLiveTracking(Long passengerId) {

        validateChildAccess(passengerId);

        // 🔴 1. Get passenger trip
        TripPassengerExecution passengerTrip = tripPassengerExecutionRepository
                .findTopByPassengerIdOrderByCreatedAtDesc(passengerId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        TripExecution trip = passengerTrip.getTripExecution();

        // 🔴 2. Get stops
        List<TripStopExecution> stops = tripStopExecutionRepository
                .findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

        // 🔴 3. Stop DTO
        List<StopEtaDto> stopDtos = stops.stream()
                .map(stop -> StopEtaDto.builder()
                        .stopId(stop.getId())
                        .stopName(stop.getStopNameSnapshot())
                        .sequenceNo(stop.getStopSequence())
                        .status(stop.getStopStatus())
                        .eta(format(stop.getEtaTime()))
                        .build()
                )
                .collect(Collectors.toList());

        // 🔴 4. Get route geojson
        TransportRoute route = transportRouteRepository
                .findById(trip.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

       
        String polyline = route.getRouteGeoJson();

        // 🔴 6. Get device name using vehicleId
        Long vehicleId = trip.getActualVehicleId() != null
                ? trip.getActualVehicleId()
                : trip.getPlannedVehicleId();

        String vehicleNumber = null;

        if (vehicleId != null) {
            DeviceEntity device = deviceRepository.findById(vehicleId).orElse(null);
            if (device != null) {
                vehicleNumber = device.getName();
            }
        }

        // 🔴 7. Current location (fallback from last stop)
        LocationDto currentLocation = LocationDto.builder()
                .latitude(stops.isEmpty() ? 0.0 : stops.get(0).getLatitude())
                .longitude(stops.isEmpty() ? 0.0 : stops.get(0).getLongitude())
                .address("Current Location")
                .build();

        return LiveTrackingResponse.builder()
                .tripId(trip.getId())
                .routeId(trip.getRouteId())
                .vehicleId(vehicleId)
                .vehicleNumber(vehicleNumber)
                .tripStatus(trip.getTripStatus())
                .currentLocation(currentLocation)
                .speedKph(0) // later from GPS
                .lastUpdateTime(passengerTrip.getUpdatedAt() != null
                        ? passengerTrip.getUpdatedAt().toString()
                        : null)
                .schoolEta(null) // optional later
                .routeGeoJson(polyline)
                .stops(stopDtos)
                .build();
    }

    @Override
    public ChildTripStatusResponse getChildTripStatus(Long passengerId) {
        validateChildAccess(passengerId);
        return ChildTripStatusResponse.builder()
                .tripId(0L)
                .tripDate("TODO")
                .routeType("Pickup")
                .attendanceStatus("NOT_MARKED")
                .boardingStatus("PENDING")
                .deboardingStatus("PENDING")
                .build();
    }
    @Transactional
    @Override
    public TripScheduleResponse getSchedule(Long passengerId) {

        validateChildAccess(passengerId);

        // 1️⃣ Get latest passenger trip
        TripPassengerExecution passenger = tripPassengerExecutionRepository
                .findFirstByPassengerIdOrderByCreatedAtDesc(passengerId);

        if (passenger == null) {
            throw new RuntimeException("No trip found for passenger");
        }

        TripExecution trip = passenger.getTripExecution();

        // 2️⃣ Get all stops
        List<TripStopExecution> stops =
                tripStopExecutionRepository.findByTripExecutionIdOrderByStopSequenceAsc(trip.getId());

        // 3️⃣ Identify pickup & drop stops
        TripStopExecution pickupStop = stops.stream()
                .filter(s -> s.getRouteStopId().equals(passenger.getPickupStopId()))
                .findFirst()
                .orElse(null);

        TripStopExecution dropStop = stops.stream()
                .filter(s -> s.getRouteStopId().equals(passenger.getDropStopId()))
                .findFirst()
                .orElse(null);

        // 4️⃣ Build Pickup DTO
        TripLegScheduleDto pickup = TripLegScheduleDto.builder()
                .routeName(trip.getRouteNameSnapshot())
                .plannedDepartureFromDepot(format(trip.getPlannedStartTime()))
                .plannedStopArrival(format(pickupStop != null ? pickupStop.getPlannedArrivalTime() : null))
                .actualBoardingTime(format(passenger.getBoardTime()))
                .plannedSchoolArrival(format(trip.getPlannedEndTime()))
                .guardianHandoverRequired(false)
                .build();

        // 5️⃣ Build Drop DTO
        TripLegScheduleDto drop = TripLegScheduleDto.builder()
                .routeName(trip.getRouteNameSnapshot())
                .plannedSchoolDeparture(format(trip.getPlannedStartTime()))
                .plannedStopArrival(format(dropStop != null ? dropStop.getPlannedArrivalTime() : null))
                .actualBoardingTime(format(passenger.getDeboardTime()))
                .guardianHandoverRequired(passenger.getGuardianVerified())
                .backupGuardianName(passenger.getGuardianNameSnapshot())
                .build();

        return TripScheduleResponse.builder()
                .pickup(pickup)
                .drop(drop)
                .build();
    }

    @Override
    public TripHistoryResponse getHistory(Long passengerId, Integer page, Integer size) {
        validateChildAccess(passengerId);
        return TripHistoryResponse.builder()
                .passengerId(passengerId)
                .history(List.of())
                .page(page)
                .size(size)
                .total(0L)
                .build();
    }

    private void validateChildAccess(Long passengerId) {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        parentChildMapRepository.findByParentIdAndChildPassengerIdAndActiveTrue(parentId, passengerId)
                .orElseThrow(() -> new SecurityException("You are not allowed to access this child"));
    }
    private String format(LocalDateTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}
