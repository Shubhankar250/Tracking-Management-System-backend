package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.TripStopExecution;

public interface TripStopExecutionRepository extends JpaRepository<TripStopExecution, Long> {

    List<TripStopExecution> findByTripExecutionIdAndIsCoveredTrue(Long tripExecutionId);
    
    @Query("""
    	    SELECT tse FROM TripStopExecution tse
    	    WHERE tse.tripExecution.id IN :tripExecutionIds
    	""")
    	List<TripStopExecution> findAllStopsByTripExecutionIds(
    	        @Param("tripExecutionIds") List<Long> tripExecutionIds
    	);

    List<TripStopExecution> findByTripExecutionIdOrderByStopSequenceAsc(Long tripExecutionId);

}