package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.MovementReportProjection;
import com.trackingpath.entities.EventData;

@Repository
public interface MovementReportRepository extends JpaRepository<EventData, Integer> {

    @Query(value = """
        SELECT 
            d.id as deviceid,
            d.name as name,
            d.device_model as device_model,
            l.address,
            l.altitude,
            l.attributes,
            l.course,
            l.latitude,
            l.longitude,
            l.speed,
            l.valid,
            l.fuellevel as fuellevel,
            extract(EPOCH from l.devicetime) as devicetime,
            extract(EPOCH from l.fixtime) as fixtime,
            extract(EPOCH from l.servertime) as servertime
        FROM devices d
        INNER JOIN eventdata l ON d.id = l.deviceid
        WHERE d.id IN (:deviceIds)
          AND l.speed > 0
          AND l.devicetime >= TO_TIMESTAMP(:startTime,'YYYY-MM-DD HH24:MI:SS')
          AND l.devicetime <= TO_TIMESTAMP(:endTime,'YYYY-MM-DD HH24:MI:SS')
          AND l.devicetime = l.fixtime
          AND (:speed IS NULL OR l.speed >= :speed)         
        ORDER BY l.devicetime ASC
        """, nativeQuery = true)
    List<MovementReportProjection> getMovementData(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("deviceIds") List<Long> deviceIds,          
            @Param("speed") Double speed
    );
    
    
    @Query(value = """
            SELECT 
                l.speed as speed,
                d.name as name,
                d.device_model as device_model,
                l.address as address,
                extract(EPOCH from l.devicetime) as devicetime,
                l.latitude as latitude,
                l.longitude as longitude
            FROM devices d
            INNER JOIN eventdata l ON d.id = l.deviceid
            WHERE d.user_id = :userId
              AND d.id IN (:deviceIds)
              AND l.devicetime >= TO_TIMESTAMP(:startTime,'YYYY-MM-DD HH24:MI:SS')
              AND l.devicetime <= TO_TIMESTAMP(:endTime,'YYYY-MM-DD HH24:MI:SS')
              AND l.speed > :speed
            ORDER BY l.devicetime ASC
            """, nativeQuery = true)
        List<Object[]> getOverspeedData(
                @Param("userId") Long userId,
                @Param("deviceIds") List<Long> deviceIds,
                @Param("startTime") String startTime,
                @Param("endTime") String endTime,
                @Param("speed") long speed
        );
}

