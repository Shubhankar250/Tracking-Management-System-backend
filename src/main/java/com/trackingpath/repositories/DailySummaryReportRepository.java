package com.trackingpath.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.EventData;

@Repository
public interface DailySummaryReportRepository extends JpaRepository<DeviceEntity, Long> {
	
	@Query(value = """
	        SELECT 
	            dr.deviceid,
	            d.name,
	            dr.total_records,
	            dr.total_distance,
	            dr.total_idle_time,
	            dr.total_movement_time,
	            dr.total_ignition_on_time,
	            dr.total_overspeed,
	            dr.date,
	            dr.maximum_speed,
	            dr.minimum_speed,
	            dr.average_speed,
	            dr.fuel_consumption
	        FROM devices d
	        INNER JOIN daily_vehicle_report dr ON d.id = dr.deviceid
	        WHERE dr.date >= :startDate
	        AND dr.date <= :endDate
	        AND (:deviceId = 0 OR dr.deviceid = :deviceId)
	        AND (
	            (:isAdmin = true AND dr.admin_id = :adminId)
	            OR
	            (:isAdmin = false AND dr.user_id = :userId)
	        )
	        """, nativeQuery = true)
	List<Object[]> getDailySummaryData(
		    @Param("deviceId") long deviceId,
		    @Param("startDate") LocalDate startDate,
		    @Param("endDate") LocalDate endDate,
		    @Param("adminId") long adminId,
		    @Param("userId") long userId,
		    @Param("isAdmin") boolean isAdmin
		);

}
