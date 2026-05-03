package com.trackingpath.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trackingpath.entities.ActivityLog;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("""
        SELECT a
        FROM ActivityLog a
        WHERE a.userId = :userId
          AND a.creationTime BETWEEN :fromTime AND :toTime
          AND (
                (:logTypes IS NOT NULL AND a.logType IN :logTypes)
             OR (:logTypes IS NULL AND a.logType LIKE CONCAT('%', :module, '%'))
          )
        ORDER BY a.creationTime DESC
    """)
    List<ActivityLog> findAll(
            @Param("userId") Long userId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("logTypes") List<String> logTypes,
            @Param("module") String module
    );
}