package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.EventData;

@Repository
public interface DistanceReportRepository extends JpaRepository<EventData, Long> {

    @Query(value = """
        SELECT e.address,
               EXTRACT(EPOCH FROM e.devicetime),
               e.latitude,
               e.longitude,
               d.name
        FROM eventdata e
        JOIN devices d ON d.id = e.deviceid
        WHERE e.deviceid = :deviceId
          AND e.devicetime >= TO_TIMESTAMP(:startTime, 'YYYY-MM-DD HH24:MI:SS')
          AND e.devicetime <= TO_TIMESTAMP(:endTime, 'YYYY-MM-DD HH24:MI:SS')
          AND e.devicetime = e.fixtime
        ORDER BY e.devicetime ASC
        """, nativeQuery = true)
    List<Object[]> getDistanceData(
            long deviceId,
            String startTime,
            String endTime
    );
}
