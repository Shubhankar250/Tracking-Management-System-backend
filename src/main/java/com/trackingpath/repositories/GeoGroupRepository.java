package com.trackingpath.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.dtos.GeoGroupDTO;

import com.trackingpath.entities.GeoGroups;

import jakarta.transaction.Transactional;

public interface GeoGroupRepository extends JpaRepository<GeoGroups, Long> {
	
	@Query(" SELECT new com.trackingpath.dtos.GeoGroupDTO(g.id, g.name) FROM GeoGroups g WHERE g.user_id = :user_id")
		List<GeoGroupDTO> findGeoGroups(@Param("user_id") Long user_id);

	
	 @Modifying(clearAutomatically = true)
	    @Transactional
	    @Query("""
	        DELETE FROM GeoGroups g
	        WHERE g.id = :id
	          AND g.user_id = :userId
	    """)
	    int deleteByIdAndUserId(@Param("id") Long id,
	                            @Param("userId") Long userId);
		
	}
	
	


	



