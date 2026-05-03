package com.trackingpath.repositories;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.LiveData;
import com.trackingpath.entities.TransportRoute;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {

	@Query("""
		    SELECT r
		    FROM TransportRoute r
		    LEFT JOIN r.shift s
		    LEFT JOIN r.defaultVehicle v
		    WHERE
		        (:search IS NULL OR :search = '' OR
		            LOWER(r.routeName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(s.shiftName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.sourceType) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		        AND
		        (:routeType IS NULL OR :routeType = '' OR
		            LOWER(r.routeType) = LOWER(:routeType)
		        )
		""")
		Page<TransportRoute> searchRoutes(
		        @Param("search") String search,
		        @Param("routeType") String routeType,
		        Pageable pageable
		);
	
	
	
	
	@Query("""
		    SELECT l FROM TransportRoute r
		    JOIN LiveData l ON l.device.id = r.defaultVehicleId
		    WHERE r.id = :routeId
		    ORDER BY l.servertime DESC
		""")
		List<LiveData> findLiveDataByRouteId(Long routeId);
	
	
	
	
	
	
	
	
	
	
	
	


	
}
