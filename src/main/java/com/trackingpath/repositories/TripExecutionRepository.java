package com.trackingpath.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.TripExecution;

public interface TripExecutionRepository extends JpaRepository<TripExecution, Long> {
	@Query("""
		    SELECT t FROM TripExecution t
		    WHERE t.routeId IN :routeIds
		    AND t.tripDate = CURRENT_DATE
		""")
		List<TripExecution> findTodayTripsByRouteIds(@Param("routeIds") List<Long> routeIds);
	Optional<TripExecution> findTopByDriverIdOrderByCreatedAtDesc(Long driverId);

}