package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.TransportShift;

public interface TransportShiftRepository extends JpaRepository<TransportShift, Long> {
}
