package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.TransportPassenger;

import java.util.List;

public interface TransportPassengerRepository extends JpaRepository<TransportPassenger, Long> {
    List<TransportPassenger> findByActiveTrueOrderByPassengerNameAsc();
    List<TransportPassenger> findByPassengerNameContainingIgnoreCase(String passengerName);
    List<TransportPassenger> findByGuardianMobileAndActiveTrue(String guardianMobile);
}
