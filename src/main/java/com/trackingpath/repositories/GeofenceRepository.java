package com.trackingpath.repositories;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import com.trackingpath.entities.Geofence;

import jakarta.transaction.Transactional;


public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
	
		@Query(value = """
			    SELECT
			        id,
			        name AS pcts_name,
			        color,
			        ST_AsGeoJSON(geom),
			        pcts_type,
			        geo_group,
			        speed_limit,
			        radius
			    FROM geofence
			    WHERE admin_id = :adminId
			    AND (
			        :search = '%%'
			        OR LOWER(name) LIKE LOWER(:search)
			        OR LOWER(pcts_type) LIKE LOWER(:search)
			        OR LOWER(geo_group) LIKE LOWER(:search)
			    )
			    ORDER BY id DESC
			""",
			countQuery = """
			    SELECT COUNT(*)
			    FROM geofence
			    WHERE admin_id = :adminId
			    AND (
			        :search = '%%'
			        OR LOWER(name) LIKE LOWER(:search)
			        OR LOWER(pcts_type) LIKE LOWER(:search)
			        OR LOWER(geo_group) LIKE LOWER(:search)
			    )
			""",
			nativeQuery = true)
			Page<Object[]> findGeofenceByAdmin(
			        @Param("adminId") Long adminId,
			        @Param("search") String search,
			        Pageable pageable
			);


			@Query(value = """
				    SELECT
				        id,
				        name AS pcts_name,
				        color,
				        ST_AsGeoJSON(geom),
				        pcts_type,
				        geo_group,
				        speed_limit,
				        radius
				    FROM geofence
				    WHERE user_id = :userId
				    AND (
				        :search = '%%'
				        OR LOWER(name) LIKE LOWER(:search)
				        OR LOWER(pcts_type) LIKE LOWER(:search)
				        OR LOWER(geo_group) LIKE LOWER(:search)
				    )
				    ORDER BY id DESC
				""",
				countQuery = """
				    SELECT COUNT(*)
				    FROM geofence
				    WHERE user_id = :userId
				    AND (
				        :search = '%%'
				        OR LOWER(name) LIKE LOWER(:search)
				        OR LOWER(pcts_type) LIKE LOWER(:search)
				        OR LOWER(geo_group) LIKE LOWER(:search)
				    )
				""",
				nativeQuery = true)
				Page<Object[]> findGeofenceByUser(
				        @Param("userId") Long userId,
				        @Param("search") String search,
				        Pageable pageable
				);
	    
	@Query(" SELECT new com.trackingpath.entities.Geofence(g.id, g.name) FROM Geofence g")
	    List<Geofence> findAllGeofence();
	
	
	  @Modifying
	    @Transactional
	    @Query(value = """
	        INSERT INTO geofence
    (name, geom, color, pcts_type, geo_group, speed_limit, radius, admin_id, user_id)
    VALUES
    (:name,
     ST_SetSRID(ST_GeomFromGeoJSON(:geom), 4326),
     :color,
     :pctsType,
     :geoGroup,
     :speedLimit,
     :radius,
     :adminId,
     :userId)
	        """, nativeQuery = true)
	    void insertGeofence(
	        @Param("name") String name,
	        @Param("geom") String geoJson,
	        @Param("color") String color,
	        @Param("pctsType") String pctsType,
	        @Param("geoGroup") String geoGroup,
	        @Param("speedLimit") String speedLimit,
	        @Param("radius") Double radius,
	        @Param("adminId") Long adminId,
	        @Param("userId") Long userId
	    );

	  
	  @Modifying
	  @Transactional
	  @Query(value = """
	      UPDATE geofence
	      SET geom = ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4326)
	      WHERE id = :id
	  """, nativeQuery = true)
	  void updateGeom(@Param("id") Long id,
	                  @Param("geojson") String geojson);

	
	

	
	
}
