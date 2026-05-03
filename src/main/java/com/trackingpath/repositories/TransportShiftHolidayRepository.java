package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.TransportShiftHoliday;

public interface TransportShiftHolidayRepository extends JpaRepository<TransportShiftHoliday, Long> {
    List<TransportShiftHoliday> findByShiftId(Long shiftId);
    void deleteByShiftId(Long shiftId);
}
