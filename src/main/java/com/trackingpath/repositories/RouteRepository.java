package com.trackingpath.repositories;

import com.trackingpath.entities.Poi;
import com.trackingpath.entities.Routes;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RouteRepository extends JpaRepository<Routes, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO routes 
        (name, description, "group", buffer_distance, geom, buffer_geom, user_id, admin_id)
        VALUES (
            :name,
            :description,
            :group,
            :buffer,
            ST_SetSRID(ST_GeomFromGeoJSON(:geom), 4326),
            ST_Buffer(ST_SetSRID(ST_GeomFromGeoJSON(:geom), 4326)::geography, :buffer)::geometry,
            :userId,
            :adminId
        )
        """, nativeQuery = true)
    void insertRoute(
        @Param("name") String name,
        @Param("description") String description,
        @Param("group") String group,
        @Param("buffer") Double buffer,
        @Param("geom") String geom,
        @Param("userId") Long userId,
        @Param("adminId") Long adminId
    );
    
    

    @Query(value = """
            SELECT 
                id,
                name,
                description,
                "group",
                buffer_distance as buffer,
                ST_AsGeoJSON(geom)        as geom,
                ST_AsGeoJSON(buffer_geom) as bufferGeom
            FROM routes
            WHERE
                :search = '%%'
                OR LOWER(name) LIKE LOWER(:search)
                OR LOWER(description) LIKE LOWER(:search)
                OR LOWER("group") LIKE LOWER(:search)
            ORDER BY id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM routes
            WHERE
                :search = '%%'
                OR LOWER(name) LIKE LOWER(:search)
                OR LOWER(description) LIKE LOWER(:search)
                OR LOWER("group") LIKE LOWER(:search)
            """,
            nativeQuery = true)
    Page<Map<String, Object>> findAllRoutesAsGeoJson(
            @Param("search") String search,
            Pageable pageable
    );

    
    
    @Query(value = """
            SELECT 
                id,
                name,
                description,
                "group",
                buffer_distance AS buffer,
                ST_AsGeoJSON(geom) AS geom,
                ST_AsGeoJSON(buffer_geom) AS bufferGeom
            FROM routes
            WHERE id = :id
            """, nativeQuery = true)
        Map<String, Object> findRouteByIdAsGeoJson(@Param("id") long id);


    @Modifying
    @Query(value = """
        UPDATE routes
        SET
            name = :name,
            description = :description,
            "group" = :groupName,
            buffer_distance = :buffer
        WHERE id = :id
    """, nativeQuery = true)
    int updateRouteWithoutGeom(
        @Param("id") Long id,
        @Param("name") String name,
        @Param("description") String description,
        @Param("groupName") String group,
        @Param("buffer") Double buffer
    );


    @Modifying
    @Query(value = """
        UPDATE routes
        SET
            name = :name,
            description = :description,
            "group" = :groupName,
            buffer_distance = :buffer,
            geom = ST_SetSRID(ST_GeomFromGeoJSON(:geom), 4326)
        WHERE id = :id
    """, nativeQuery = true)
    int updateRouteWithGeom(
        @Param("id") Long id,
        @Param("name") String name,
        @Param("description") String description,
        @Param("groupName") String group,
        @Param("buffer") Double buffer,
        @Param("geom") String geom
    );



	@Query(" SELECT new com.trackingpath.entities.Routes(r.id, r.name) FROM Routes r")
    List<Routes> findAllRoute();
}

