package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trackingpath.entities.TripVehicleEvent;

import java.util.List;
import java.util.Optional;

public interface TripVehicleEventRepository extends JpaRepository<TripVehicleEvent, Long> {

    List<TripVehicleEvent> findByTripExecutionIdOrderByEventTimeAsc(Long tripExecutionId);
    Optional<TripVehicleEvent> findTopByTripExecutionIdOrderByCreatedAtDesc(Long tripExecutionId);
    
}