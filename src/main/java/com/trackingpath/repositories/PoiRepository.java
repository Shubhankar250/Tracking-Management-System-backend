package com.trackingpath.repositories;

import com.trackingpath.entities.Poi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PoiRepository extends JpaRepository<Poi, Long> {



    List<Poi> findByPoiGroupIdInAndAdminId(List<Integer> groupIds, Long adminId);

    @Query("""
    	    SELECT p
    	    FROM Poi p
    	    WHERE p.adminId = :adminId
    	    AND (
    	        :search = '%%'
    	        OR LOWER(p.name) LIKE LOWER(:search)
    	        OR LOWER(p.description) LIKE LOWER(:search)
    	        OR LOWER(p.markerIcon) LIKE LOWER(:search)
    	    )
    	    ORDER BY p.id DESC
    	""")
    	Page<Poi> findPoiByAdminId(
    	        @Param("adminId") Long adminId,
    	        @Param("search") String search,
    	        Pageable pageable
    	);


	@Query(" SELECT new com.trackingpath.entities.Poi(p.id, p.name) FROM Poi p")
    List<Poi> findAllPoi();

}
