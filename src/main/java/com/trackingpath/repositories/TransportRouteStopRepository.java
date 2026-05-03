package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.TransportRouteStop;

public interface TransportRouteStopRepository extends JpaRepository<TransportRouteStop, Long> {
	List<TransportRouteStop> findByRouteId(Long routeId);
    List<TransportRouteStop> findByRouteIdOrderBySequenceNoAsc(Long routeId);
    void deleteByRouteId(Long routeId);
    Optional<TransportRouteStop> findByClientStopId(Long clientStopId);
    Optional<TransportRouteStop> findByRouteIdAndClientStopId(Long routeId, Long clientStopId);
}
