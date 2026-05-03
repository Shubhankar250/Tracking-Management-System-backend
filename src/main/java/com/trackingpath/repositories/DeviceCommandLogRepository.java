package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.CommandDTO;
import com.trackingpath.entities.DeviceCommandLog;

import jakarta.transaction.Transactional;

@Repository
public interface DeviceCommandLogRepository
        extends JpaRepository<DeviceCommandLog, Long> {
	@Query("""
		    SELECT new com.trackingpath.dtos.CommandDTO(
		        d.name,
		        dcl.commandName,
		        dcl.commandMsg,
		        dcl.createdOn,
		        d.devicetimezone,
		        dcl.commandCategory,
		        dcl.commandSubCategory,
		        dcl.deviceId
		    )
		    FROM DeviceCommandLog dcl
		    JOIN dcl.device d
		    WHERE
		        (
		            (:isAdmin = true AND dcl.adminId = :adminId)
		            OR
		            (:isAdmin = false AND dcl.userId = :userId)
		        )
		        AND
		        (
		            :search IS NULL OR :search = '' OR
		            LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(dcl.commandName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(dcl.commandMsg) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		    ORDER BY dcl.id DESC
		""")
		Page<CommandDTO> findCommandLogs(
		        @Param("isAdmin") boolean isAdmin,
		        @Param("adminId") Long adminId,
		        @Param("userId") Long userId,
		        @Param("search") String search,
		        Pageable pageable
		);
	
	
	@Modifying
	@Transactional
	@Query(value = """
	    UPDATE device_command_log 
	    SET response = :response
	    WHERE id = (
	        SELECT id FROM (
	            SELECT id 
	            FROM device_command_log 
	            WHERE device_id = :deviceId 
	            ORDER BY id DESC 
	            LIMIT 1
	        ) AS temp
	    )
	    """, nativeQuery = true)
	int updateLatestResponse(@Param("deviceId") Long deviceId,
	                         @Param("response") String response);
	
	
	@Modifying
	@Query(value = """
	    UPDATE device_command_log 
	    SET response = :response
	    WHERE id = (
	        SELECT id FROM (
	            SELECT id 
	            FROM device_command_log 
	            WHERE device_id = :deviceId 
	              AND channel_id = :channelId
	            ORDER BY id DESC 
	            LIMIT 1
	        ) temp
	    )
	    """, nativeQuery = true)
	int updateLatestResponseByDeviceAndChannel(Long deviceId,
	                                           Integer channelId,
	                                           String response);





	@Query(value = "SELECT * FROM device_command_log " +
            "WHERE device_id = :deviceId " +
            "AND created_on BETWEEN CAST(:start AS TIMESTAMP) AND CAST(:end AS TIMESTAMP) " +
            "AND (:channel IS NULL OR :channel = 0 OR channel_id = :channel) " +
            "ORDER BY created_on ASC",
    nativeQuery = true)
List<DeviceCommandLog> getSnapshotDataWithChannel(
     @Param("deviceId") int deviceId,
     @Param("start") String start,
     @Param("end") String end,
     @Param("channel") Integer channel
	);
	@Query("""
		    SELECT d FROM DeviceCommandLog d
		    WHERE d.deviceId = :deviceId
		    AND d.commandSubCategory = 'TERMINAL_CONFIGURATION'
		    ORDER BY d.createdOn DESC
		""")
		List<DeviceCommandLog> findLatestTerminalConfig(@Param("deviceId") Long deviceId);	
	
}