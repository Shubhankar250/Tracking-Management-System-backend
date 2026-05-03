package com.trackingpath.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.EventData;

public interface EventDataRepository extends JpaRepository<EventData, Integer> {
	@Query("""
	        SELECT e FROM EventData e
	        WHERE e.deviceId = :deviceId
	        AND e.deviceTime >= :startTime
	        AND e.deviceTime <= :endTime
	        AND e.deviceTime = e.fixTime
	        AND e.speed > 0
	        ORDER BY e.deviceTime ASC
	""")
	List<EventData> getPlaybackDataForDrivingData(
	        @Param("deviceId") Long deviceId,
	        @Param("startTime") LocalDateTime startTime,
	        @Param("endTime") LocalDateTime endTime
	);

}
