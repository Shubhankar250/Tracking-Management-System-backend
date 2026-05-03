package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.trackingpath.entities.TransportPassengerRouteAssignment;

public interface TransportPassengerRouteAssignmentRepository extends JpaRepository<TransportPassengerRouteAssignment, Long> {
    List<TransportPassengerRouteAssignment> findByRouteIdAndActiveTrue(Long routeId);
    Optional<TransportPassengerRouteAssignment> findByPassengerIdAndActiveTrue(Long passengerId);
    Optional<TransportPassengerRouteAssignment> findByUsernameAndActiveTrue(String username);

    Optional<TransportPassengerRouteAssignment> findByUsername(String username);
    
    Optional<TransportPassengerRouteAssignment>findByPassengerIdAndRouteId(Long passengerId, Long routeId);
    
    void deleteByRouteId(Long routeId);
    @Query("""
    	    SELECT a FROM TransportPassengerRouteAssignment a
    	    JOIN FETCH a.route r
    	    LEFT JOIN FETCH r.defaultVehicle
    	    LEFT JOIN FETCH a.pickupStop
    	    WHERE a.passenger.id = :passengerId AND a.active = true
    	""")
    	Optional<TransportPassengerRouteAssignment> findFullAssignment(Long passengerId);
    
    @Query("""
    	    SELECT a FROM TransportPassengerRouteAssignment a
    	    JOIN FETCH a.passenger p
    	    JOIN FETCH a.route r
    	    LEFT JOIN FETCH r.defaultVehicle v
    	    LEFT JOIN FETCH a.pickupStop s
    	    LEFT JOIN FETCH r.shift sh
    	    WHERE p.guardianMobile = :mobile 
    	      AND a.active = true
    	""")
    	List<TransportPassengerRouteAssignment> findAllByGuardianMobile(String mobile);
}
