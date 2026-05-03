package com.trackingpath.repositories;

import com.trackingpath.entities.TripPassengerExecution;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripPassengerExecutionRepository
        extends JpaRepository<TripPassengerExecution, Long> {

	@Query("""
		    SELECT tpe FROM TripPassengerExecution tpe
		    LEFT JOIN FETCH tpe.tripStopExecution tse
		    WHERE tpe.passengerId IN :passengerIds
		    AND tpe.tripExecution.routeId IN :routeIds
		    AND tpe.tripExecution.tripDate = CURRENT_DATE
		""")
		List<TripPassengerExecution> findTodayData(
		        @Param("passengerIds") List<Long> passengerIds,
		        @Param("routeIds") List<Long> routeIds
		);
	
	  @Query("""
	    	    SELECT tpe FROM TripPassengerExecution tpe
	    	    LEFT JOIN FETCH tpe.tripStopExecution tse
	    	    WHERE tpe.tripExecution.id = :tripId
	    	""")
	    	List<TripPassengerExecution> findByTripExecutionIdWithStop(@Param("tripId") Long tripId);

	  @Query("""
			    SELECT tpe FROM TripPassengerExecution tpe
			    LEFT JOIN FETCH tpe.tripStopExecution tse
			    WHERE tpe.tripExecution.id = :tripId
			    AND LOWER(tpe.pickupStopNameSnapshot) = LOWER(:pickupStop)
			""")
			List<TripPassengerExecution> findByTripIdAndPickupStopWithStop(
			        @Param("tripId") Long tripId,
			        @Param("pickupStop") String pickupStop
			);
	  @Query("""
			    SELECT tpe FROM TripPassengerExecution tpe
			    LEFT JOIN FETCH tpe.tripStopExecution tse
			    WHERE tpe.tripExecution.id = :tripId
			    AND LOWER(tpe.dropStopNameSnapshot) = LOWER(:dropStop)
			""")
			List<TripPassengerExecution> findByTripIdAndDropStopWithStop(
			        @Param("tripId") Long tripId,
			        @Param("dropStop") String dropStop
			);
	  @Query("""
			    SELECT tpe FROM TripPassengerExecution tpe
			    LEFT JOIN FETCH tpe.tripStopExecution tse
			    WHERE tpe.tripExecution.id = :tripId
			    AND LOWER(tpe.attendanceStatus) = LOWER(:status)
			""")
			List<TripPassengerExecution> findByTripIdAndStatusWithStop(
			        @Param("tripId") Long tripId,
			        @Param("status") String status
			);
	  @Query("""
			    SELECT tpe FROM TripPassengerExecution tpe
			    LEFT JOIN FETCH tpe.tripStopExecution tse
			    WHERE tpe.tripExecution.id = :tripId
			    AND LOWER(tpe.pickupStopNameSnapshot) = LOWER(:pickupStop)
			    AND LOWER(tpe.attendanceStatus) = LOWER(:status)
			""")
			List<TripPassengerExecution> findByTripIdPickupAndStatusWithStop(
			        @Param("tripId") Long tripId,
			        @Param("pickupStop") String pickupStop,
			        @Param("status") String status
			);
	  @Query("""
			    SELECT tpe FROM TripPassengerExecution tpe
			    LEFT JOIN FETCH tpe.tripStopExecution tse
			    WHERE tpe.tripExecution.id = :tripId
			    AND LOWER(tpe.dropStopNameSnapshot) = LOWER(:dropStop)
			    AND LOWER(tpe.attendanceStatus) = LOWER(:status)
			""")
			List<TripPassengerExecution> findByTripIdDropAndStatusWithStop(
			        @Param("tripId") Long tripId,
			        @Param("dropStop") String dropStop,
			        @Param("status") String status
			);
	// Optional version (recommended for service layer)
	  Optional<TripPassengerExecution> findTopByPassengerIdOrderByCreatedAtDesc(Long passengerId);

	  // Direct entity version
	  TripPassengerExecution findFirstByPassengerIdOrderByCreatedAtDesc(Long passengerId);
	  
}