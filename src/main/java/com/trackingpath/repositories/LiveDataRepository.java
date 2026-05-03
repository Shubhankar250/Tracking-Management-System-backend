package com.trackingpath.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.ExternalLiveDataDTO;
import com.trackingpath.dtos.LiveDataProjection;
import com.trackingpath.dtos.LiveDataView;
import com.trackingpath.entities.LiveData;

@Repository
public interface LiveDataRepository extends JpaRepository<LiveData, Long> {
	Optional<LiveData> findByDeviceId(Long deviceId);

	@Query(value = """
						SELECT d.id AS device_id,d.devicetimezone,d.uniqueid as uniqueid,d.sim_card_number as sim_card_number,d.device_model as device_model, d.name AS device_name, d.object_icon AS object_icon,
						       d.status AS status, d.vehicle_status AS vehicle_status,
						       g.name AS group_name, g.id AS group_id,
						       l.devicetime AS devicetime,
						       l.servertime AS servertime,
						       l.lastidletime AS lastidletime,
						       l.lastmovementtime AS lastmovementtime,
						       l.attributes, l.course, l.latitude, l.longitude, l.altitude, l.speed, l.address,
						       d.img_icon_name, d.img_icon_type, d.icon_type, d.moving_icon_color,
						       d.stopped_icon_color, d.offline_icon_color, d.engine_idle_color,
						     d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type AS modalType

						FROM livedata l
						JOIN devices d ON d.id = l.deviceid
						LEFT JOIN (
			    SELECT DISTINCT ON (device_id)
			        device_id,
			        group_id
			    FROM devices_group_mapping
			    ORDER BY device_id, creation_time DESC
			) dgm ON dgm.device_id = d.id
						LEFT JOIN groups g ON g.id = dgm.group_id
						 LEFT JOIN device_modal dm ON dm.modal_name = d.device_model

						WHERE d.vehicle_status = 'ACTIVE'
						GROUP BY d.id, d.name,d.uniqueid,d.sim_card_number,d.device_model, d.status, g.name, g.id,
						         l.devicetime, l.deviceid, l.servertime,
						         l.lastidletime, l.lastmovementtime,
						         l.attributes, l.course, l.latitude, l.longitude,
						         l.altitude, l.speed, l.address,
						         d.img_icon_name, d.img_icon_type, d.icon_type,
						         d.moving_icon_color, d.stopped_icon_color,
						         d.offline_icon_color, d.engine_idle_color,
						         d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type
						""", nativeQuery = true)
	List<LiveDataView> findLiveData(@Param("stime") String stime, @Param("etime") String etime);

	@Query(value = """
						SELECT d.id AS device_id,d.devicetimezone,d.uniqueid as uniqueid,d.sim_card_number as sim_card_number,d.device_model as device_model, d.name AS device_name, d.object_icon AS object_icon,
						       d.status AS status, d.vehicle_status AS vehicle_status,
						       g.name AS group_name, g.id AS group_id,
						       l.devicetime AS devicetime,
						       l.servertime AS servertime,
						       l.lastidletime AS lastidletime,
						       l.lastmovementtime AS lastmovementtime,
						       l.attributes, l.course, l.latitude, l.longitude, l.altitude, l.speed, l.address,
						       d.img_icon_name, d.img_icon_type, d.icon_type, d.moving_icon_color,
						       d.stopped_icon_color, d.offline_icon_color, d.engine_idle_color,
						     d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type AS modalType,

						       COALESCE((
						           SELECT json_agg(json_build_object(
						               'name', drv.name,
						               'rfid', drv.rfid,
						               'phone', drv.phone,
						               'email', drv.email))
						           FROM driveres drv WHERE drv.device_id = d.id
						       ), '[]') AS drivers,

						       COALESCE((
						           SELECT json_agg(json_build_object(
						               'service_name', ms.service_name,
						               'last_service_km', ms.last_service_km,
						               'last_service_date', ms.last_service_date))
						           FROM maintenance_service ms WHERE ms.device_id = d.id
						       ), '[]') AS services,

						       COALESCE((
						           SELECT json_agg(row_to_json(ev))
						           FROM (
						               SELECT TO_CHAR(e.alert_time, 'YYYY-MM-DD HH24:MI:SS') AS alert_time,
						                      e.alert_type
						               FROM events e
						               WHERE e.device_id = l.deviceid
						                 AND e.alert_time >= TO_TIMESTAMP(:stime,'YYYY-MM-DD HH24:MI:SS')
						                 AND e.alert_time <= TO_TIMESTAMP(:etime,'YYYY-MM-DD HH24:MI:SS')
						               ORDER BY e.alert_time DESC
						               LIMIT 3
						           ) ev
						       ), '[]') AS events

						FROM livedata l
						JOIN devices d ON d.id = l.deviceid
						LEFT JOIN (
			    SELECT DISTINCT ON (device_id)
			        device_id,
			        group_id
			    FROM devices_group_mapping
			    ORDER BY device_id, creation_time DESC
			) dgm ON dgm.device_id = d.id
						LEFT JOIN groups g ON g.id = dgm.group_id
						 LEFT JOIN device_modal dm ON dm.modal_name = d.device_model

						WHERE d.vehicle_status = 'ACTIVE'
						AND (
			    d.user_id = :userId
			    OR (
			        :deviceIds IS NOT NULL
			        AND d.id IN (:deviceIds)
			    )
			)

						GROUP BY d.id, d.name,d.uniqueid,d.sim_card_number,d.device_model, d.status, g.name, g.id,
						         l.devicetime, l.deviceid, l.servertime,
						         l.lastidletime, l.lastmovementtime,
						         l.attributes, l.course, l.latitude, l.longitude,
						         l.altitude, l.speed, l.address,
						         d.img_icon_name, d.img_icon_type, d.icon_type,
						         d.moving_icon_color, d.stopped_icon_color,
						         d.offline_icon_color, d.engine_idle_color,
						         d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type
						""", nativeQuery = true)
	List<LiveDataView> findLiveDatabyDeviceIds(@Param("deviceIds") List<Long> deviceIds, @Param("stime") String stime,
			@Param("etime") String etime, @Param("userId") Long userId);

	@Query("""
			    SELECT l
			    FROM LiveData l
			    JOIN l.device d
			    WHERE d.userId = :userId
			""")
	List<LiveData> findLiveDataByUser(@Param("userId") Long userId);

	@Query("""
			    SELECT l
			    FROM LiveData l
			    WHERE l.devicetime BETWEEN :start AND :end
			      AND l.latitude IS NOT NULL
			      AND l.longitude IS NOT NULL
			      AND l.latitude <> 0
			      AND l.longitude <> 0
			    ORDER BY l.devicetime
			""")
	List<LiveData> findTodayData(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query("""
			    SELECT l FROM LiveData l
			    WHERE l.devicetime BETWEEN :start AND :end
			      AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL
			      AND l.latitude <> 0 AND l.longitude <> 0
			    ORDER BY l.devicetime
			""")
	List<LiveData> findPointsForToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query(value = """
			    SELECT l.latitude,
			           l.longitude,
			           d.name AS vehicle_name,
			           a.device_id,
			           l.speed,
			           l.address,
			           EXTRACT(EPOCH FROM l.devicetime) AS devicetime,
				        d.status AS status
			    FROM api_manager a
			    JOIN devices d ON d.id::text = ANY(string_to_array(a.device_id, ','))
			    JOIN livedata l ON l.deviceid = d.id
			    WHERE a.access_code = :accessCode
			""", nativeQuery = true)
	List<Object[]> findExternalLiveDataRaw(@Param("accessCode") Long accessCode);

	@Query("""
			    SELECT DISTINCT
			        d.id AS deviceId,
			        d.name AS deviceName,
			        d.status AS status,
			        d.uniqueid as uniqueid,
			        g.id AS groupId,
			        g.name AS groupName,
			        l.devicetime AS deviceTime,
			        l.servertime AS serverTime,
			        l.lastidletime AS lastIdleTime,
			        l.lastmovementtime AS lastMovementTime,
			        l.latitude AS latitude,
			        l.longitude AS longitude,
			        l.altitude AS altitude,
			        l.speed AS speed,
			        l.course AS course,
			        l.address AS address,
			        l.attributes AS attributes,
			        d.iconType AS iconType,
			        d.movingIconColor AS movingIconColor,
			        d.stoppedIconColor AS stoppedIconColor,
			        d.offlineIconColor AS offlineIconColor,
			        d.engineIdleColor AS engineIdleColor,
			        d.imgIconName AS imgIconName,
			        d.imgIconType AS imgIconType,
			        dm.noOfChannel AS channelNo
			    FROM LiveData l
			    JOIN l.device d

			    LEFT JOIN DeviceGroupMapping dgm
			        ON dgm.id = (
			            SELECT MAX(dgm2.id)
			            FROM DeviceGroupMapping dgm2
			            WHERE dgm2.device = d AND dgm2.userId = :userId
			        )

			    LEFT JOIN dgm.group g
			    LEFT JOIN DeviceModalEntity dm ON dm.modalName = d.deviceModel

			    WHERE d.id = :deviceId
			""")
	Optional<LiveDataProjection> findLiveDataByDeviceId(@Param("deviceId") Long deviceId, @Param("userId") Long userId);

	@Query(value = """
			    SELECT d.id AS device_id, d.name AS device_name, d.uniqueid as uniqueid,d.sim_card_number as sim_card_number,d.device_model as device_model,d.object_icon AS object_icon,
			           d.status AS gps_status, d.vehicle_status AS vehicle_status,
			           g.name AS group_name, g.id AS group_id,
			           l.devicetime AS devicetime,
			           l.servertime AS servertime,
			           l.lastidletime AS lastidletime,
			           l.lastmovementtime AS lastmovementtime,
			           l.attributes, l.course, l.latitude, l.longitude, l.altitude, l.speed, l.address,
			           d.img_icon_name, d.img_icon_type, d.icon_type, d.moving_icon_color,
			           d.stopped_icon_color, d.offline_icon_color, d.engine_idle_color,
			           d.status, d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type AS modalType,

			           COALESCE((
			               SELECT json_agg(json_build_object(
			                   'name', drv.name,
			                   'rfid', drv.rfid,
			                   'phone', drv.phone,
			                   'email', drv.email))
			               FROM driveres drv WHERE drv.device_id = d.id
			           ), '[]') AS drivers,

			           COALESCE((
			               SELECT json_agg(json_build_object(
			                   'service_name', ms.service_name,
			                   'last_service_km', ms.last_service_km,
			                   'last_service_date', ms.last_service_date))
			               FROM maintenance_service ms WHERE ms.device_id = d.id
			           ), '[]') AS services,

			           COALESCE((
			               SELECT json_agg(row_to_json(ev))
			               FROM (
			                   SELECT TO_CHAR(e.alert_time, 'YYYY-MM-DD HH24:MI:SS') AS alert_time,
			                          e.alert_type
			                   FROM events e
			                   WHERE e.device_id = l.deviceid
			                     AND e.alert_time >= TO_TIMESTAMP(:stime,'YYYY-MM-DD HH24:MI:SS')
			                     AND e.alert_time <= TO_TIMESTAMP(:etime,'YYYY-MM-DD HH24:MI:SS')
			                   ORDER BY e.alert_time DESC
			                   LIMIT 3
			               ) ev
			           ), '[]') AS events

			    FROM livedata l
			    JOIN devices d ON d.id = l.deviceid
			    LEFT JOIN devices_group_mapping dgm ON dgm.device_id = d.id
			    LEFT JOIN groups g ON g.id = dgm.group_id
			    LEFT JOIN device_modal dm ON dm.modal_name = d.device_model


			    WHERE d.vehicle_status = 'ACTIVE'
			      AND d.id = :deviceId
			    GROUP BY d.id, d.name, d.uniqueid,d.status, g.name, g.id,
			             l.devicetime, l.deviceid, l.servertime,
			             l.lastidletime, l.lastmovementtime,
			             l.attributes, l.course, l.latitude, l.longitude,
			             l.altitude, l.speed, l.address,
			             d.img_icon_name, d.img_icon_type, d.icon_type,
			             d.moving_icon_color, d.stopped_icon_color,
			             d.offline_icon_color, d.engine_idle_color,
			             d.tail_color, d.tail_length, d.min_moving_speed,dm.modal_type,d.sim_card_number,d.device_model
			""", nativeQuery = true)
	LiveDataView findLiveDataByDevice(@Param("deviceId") Long deviceId, @Param("stime") String stime,
			@Param("etime") String etime);

	List<LiveData> findByDevice_IdInAndDevicetimeIsNotNullOrderByDevicetimeDesc(List<Long> deviceIds);

	List<LiveData> findByDevice_IdInAndDevice_UniqueidAndDevicetimeIsNotNullOrderByDevicetimeDesc(
	        List<Long> deviceIds, String uniqueid);
	
}
