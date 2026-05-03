package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.dtos.MaintenanceAllDataView;
import com.trackingpath.dtos.MaintenanceServiceDto;
import com.trackingpath.dtos.MaintenanceView;
import com.trackingpath.entities.MaintenanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, Long> {

	List<MaintenanceView> findTop1ByDevice_IdOrderByIdDesc(Long deviceId);

	@Query(value = """
			     SELECT
			         m.id AS id,
			         m.serviceName AS serviceName,

			         d.id AS deviceId,
			         d.name AS deviceName,

			         m.datalist AS datalist,
			         m.popup AS popup,
			         m.odometerIntervalKm AS odometerIntervalKm,
			         m.engineHourInterval AS engineHourInterval,
			         m.daysInterval AS daysInterval,
			         m.odometerLeftKm AS odometerLeftKm,
			         m.engineHoursLeft AS engineHoursLeft,
			         m.updateLastService AS updateLastService,
			         m.daysLeft AS daysLeft,
			         m.eventTrigger AS eventTrigger,

			         m.odometerIntervalKmVal AS odometerIntervalKmVal,
			         m.lastServiceKm AS lastServiceKm,
			         m.engineHourIntervalVal AS engineHourIntervalVal,
			         m.lastServiceHours AS lastServiceHours,
			         m.daysIntervalVal AS daysIntervalVal,
			         m.odometerLeftKmVal AS odometerLeftKmVal,
			         m.engineHoursLeftVal AS engineHoursLeftVal,
			         m.daysLeftVal AS daysLeftVal,

			         m.lastServiceDate AS lastServiceDate,

			         u.id AS userId,
			         u.username AS username,

			         a.id AS adminId,
			         a.username AS adminName
			     FROM MaintenanceEntity m
			     JOIN m.device d
			     LEFT JOIN m.user u
			     LEFT JOIN m.admin a
			     WHERE m.user.id = :userId
			       AND (
			             :search IS NULL OR :search = '' OR
			             LOWER(m.serviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			             LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			             LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
			 LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR

			CAST(m.odometerLeftKmVal AS string) LIKE CONCAT('%', :search, '%') OR
			CAST(m.engineHoursLeftVal AS string) LIKE CONCAT('%', :search, '%') OR
			CAST(m.daysLeftVal AS string) LIKE CONCAT('%', :search, '%') OR

			CAST(m.lastServiceDate AS string) LIKE CONCAT('%', :search, '%') )
			 """, countQuery = """
			    SELECT COUNT(m.id)
			    FROM MaintenanceEntity m
			    JOIN m.device d
			    LEFT JOIN m.user u
			    LEFT JOIN m.admin a
			    WHERE m.user.id = :userId
			      AND (
			            :search IS NULL OR :search = '' OR
			            LOWER(m.serviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			            LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			            LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
			            LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
			      )
			""")
	Page<MaintenanceAllDataView> findAllMaintenance(@Param("userId") Long userId, @Param("search") String search,
			Pageable pageable);

	@Query("SELECT COUNT(m) FROM MaintenanceEntity m WHERE m.user.id = :userId AND m.lastServiceDate IS NOT NULL AND m.lastServiceDate > CURRENT_DATE ")
	Long countDue(Long userId);

	@Query("SELECT COUNT(m) FROM MaintenanceEntity m WHERE m.user.id = :userId AND m.lastServiceDate IS NOT NULL AND m.lastServiceDate < CURRENT_DATE")
	Long countOverdue(Long userId);

	@Query("""
			    SELECT new com.trackingpath.dtos.MaintenanceServiceDto(
			        m.id,
			        d.id,
			        d.name,
			        m.serviceName,
			        m.odometerIntervalKmVal,
			        m.odometerLeftKmVal,
			        m.engineHourIntervalVal,
			        m.engineHoursLeftVal,
			        m.daysIntervalVal,
			        m.daysLeftVal,
			        m.eventTrigger
			    )
			    FROM MaintenanceEntity m
			    JOIN m.device d
			    WHERE d.id = :deviceId
			""")
	List<MaintenanceServiceDto> findMaintenanceByDeviceId(@Param("deviceId") Long deviceId);

	@Query("""
			    SELECT m
			    FROM MaintenanceEntity m
			    LEFT JOIN FETCH m.device
			    LEFT JOIN FETCH m.user
			    LEFT JOIN FETCH m.admin
			    WHERE m.id = :id
			""")
	Optional<MaintenanceEntity> findByIdWithRelations(@Param("id") Long id);

}
