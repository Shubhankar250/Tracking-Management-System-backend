package com.trackingpath.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.Events;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long> {

	long countByUserId(Long userId);

	@Query("""
			    SELECT e.alertType, COUNT(e)
			    FROM Events e
			    WHERE e.userId = :userId
			    GROUP BY e.alertType
			""")
	List<Object[]> findAlertTypeCounts(@Param("userId") Long userId);


		List<Events> findTop3ByDevice_IdAndAlertTimeBetweenOrderByAlertTimeDesc(Long deviceId, LocalDateTime start,
				LocalDateTime end);
	// query for get events list
		@Query(value = """
			    SELECT
			        d.name AS device_name,
			        d.status,
			        d.devicetimezone,
			        e.attributes,
			        e.course,
			        e.altitude,
			        e.speed,
			        EXTRACT(EPOCH FROM 
  (e.alert_time AT TIME ZONE COALESCE(d.devicetimezone, 'UTC'))
) AS alert_time,
			        e.alert_type,
			        e.latitude,
			        e.longitude,
			        e.address,
			        e.message,
			        e.device_id,
			        e.user_id
			    FROM events e
			    JOIN devices d ON d.id = e.device_id
			    WHERE 
			    (
			      e.alert_time AT TIME ZONE 
			        COALESCE(d.devicetimezone, 'UTC')
			    )
			    BETWEEN
			      TO_TIMESTAMP(:start, 'YYYY-MM-DD HH24:MI:SS')
			    AND
			      TO_TIMESTAMP(:end, 'YYYY-MM-DD HH24:MI:SS')

			      AND (:userId IS NULL OR e.user_id = :userId)
			      AND (:adminId IS NULL OR d.user_id = :adminId)
			      AND (
			            CAST(:deviceIds AS bigint[]) IS NULL
			            OR e.device_id = ANY(CAST(:deviceIds AS bigint[]))
			      )
			      AND (
			            CAST(:alertTypes AS text[]) IS NULL
			            OR e.alert_type = ANY(CAST(:alertTypes AS text[]))
			      )
			  ORDER BY 
(
  e.alert_time AT TIME ZONE COALESCE(d.devicetimezone, 'UTC')
) DESC
			    """, nativeQuery = true)
			List<Object[]> fetchAlertNotifications(
			        @Param("start") String start,
			        @Param("end") String end,
			        @Param("userId") Long userId,
			        @Param("adminId") Long adminId,
			        @Param("alertTypes") String[] alertTypes,
			        @Param("deviceIds") Long[] deviceIds
			);
			
			
			@Query(value = """
				    SELECT
				        d.name AS device_name,
				        d.status,
				        EXTRACT(EPOCH FROM 
				            (e.alert_time AT TIME ZONE COALESCE(d.devicetimezone, 'UTC'))
				        ) AS alert_time,
				        e.alert_name as alert_type,
				        e.latitude,
				        e.longitude,
				        e.address,
				        e.message,
				        e.device_id,
				        e.user_id,
				        sound_notification,
				        popup_notification,
				        notification_color,
				        e.speed
				    FROM events e
				    JOIN devices d ON d.id = e.device_id
				    LEFT JOIN livedata l ON l.deviceid = d.id
				    LEFT JOIN alert_device_mapping as adm ON e.device_id = adm.device_id
				    LEFT JOIN alert_notification as an ON adm.alert_id = an.alert_id
				    WHERE 
				    (
				      e.alert_time AT TIME ZONE 
				        COALESCE(d.devicetimezone, 'UTC')
				    )
				    BETWEEN
				      TO_TIMESTAMP(:start,'YYYY-MM-DD HH24:MI:SS')
				    AND
				      TO_TIMESTAMP(:end,'YYYY-MM-DD HH24:MI:SS')

				      AND (:userId IS NULL OR e.user_id = :userId)
				      AND (:adminId IS NULL OR d.user_id = :adminId)

				    ORDER BY 
				    (
				      e.alert_time AT TIME ZONE COALESCE(d.devicetimezone, 'UTC')
				    ) DESC
				""", nativeQuery = true)
	List<Object[]> getAlertNotificationPopUp(@Param("start") String start, @Param("end") String end,
			@Param("userId") Long userId, @Param("adminId") Long adminId);

	
}
