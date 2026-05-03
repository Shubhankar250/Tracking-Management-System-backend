package com.trackingpath.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.DeviceInfoProjection;
import com.trackingpath.dtos.DevicesUpdateDto;
import com.trackingpath.dtos.SetupDeviceDTO;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Users;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long>, DeviceRepositoryCustom {
	long countByUserId(Long user_id);

	@Query("SELECT d.id AS id, d.devicetimezone AS devicetimezone FROM DeviceEntity d")
	List<DeviceTimezoneView> findAllDeviceTimezones();

	@Query("select d.id, d.name from DeviceEntity d where d.userId = :userId")
	List<Object[]> findIdAndNameByUserId(@Param("userId") Long userId);

	List<DeviceEntity> findByUserIdAndVehicleStatus(long userId, String vehicleStatus);

	List<DeviceEntity> findByIdInAndVehicleStatus(List<Long> ids, String vehicleStatus);

	@Query(value = """
		    SELECT d.id as deviceId,
		           l.address,
		           l.altitude,
		           l.attributes,
		           l.course,
		           l.fuellevel,
		           l.devicetime AS dt,

		           CAST(EXTRACT(EPOCH FROM l.devicetime) AS BIGINT) AS deviceTime,
		           CAST(EXTRACT(EPOCH FROM l.fixtime) AS BIGINT) AS fixTime,
		           l.latitude,
		           l.longitude,
		           d.name AS name,
		           l.protocol,
		           CAST(EXTRACT(EPOCH FROM l.servertime) AS BIGINT) AS serverTime,

		           l.speed,
		           l.valid,
		           et.alert_name,
		           CAST(EXTRACT(EPOCH FROM et.alert_time) AS BIGINT) AS alert_time,
		           d.devicetimezone
		      FROM devices d
		INNER JOIN eventdata l ON d.id = l.deviceid
		 LEFT JOIN events et ON l.devicetime = et.alert_time

		     WHERE d.id = :deviceId
		       AND l.devicetime >= TO_TIMESTAMP(:startTime, 'YYYY-MM-DD HH24:MI:SS')
		       AND l.devicetime <= TO_TIMESTAMP(:endTime, 'YYYY-MM-DD HH24:MI:SS')
		       AND l.devicetime = l.fixtime
		  ORDER BY l.devicetime ASC
		    """, nativeQuery = true)
List<Object[]> getPlaybackData(@Param("deviceId") long deviceId, @Param("startTime") String startTime,
		@Param("endTime") String endTime);
	
@Query("""
	    SELECT new com.trackingpath.dtos.DevicesUpdateDto(
	        d.id,
	        d.devicetimezone,
	        d.name,
	        d.deviceModel,
	        d.objectIcon,
	        d.uniqueid,
	        d.odometer,
	        d.vehicleStatus,

	        g.id,
	        g.name,

	        CONCAT(u.username,'[ ',u.firstname,' ',u.lastname,' ]'),
	        u.id,
	        u.accountname,

	        d.simCardNumber,
	        d.simActivationDate,
	        d.simExpirationDate,

	        d.vin,
	        d.installationDate,
	        d.plateNumber,
	        d.registrationNumber,
	        d.owner,

	        d.fuelMeasureName,
	        d.fuelMeasurement,
	        d.fuelCost,

	        d.iconType,
	        d.movingIconColor,
	        d.stoppedIconColor,
	        d.offlineIconColor,
	        d.engineIdleColor,

	        d.tailColor,
	        d.tailLength,
	        d.imgIconName,
	        d.imgIconType,

	        d.maxSpeed,
	        d.minMovingSpeed,
	        d.minFuelFillings,
	        d.minFuelTheft,
	        d.fuelChangeAfterStop,
	        d.rcPath,
            d.insurancePath
	    )
	    FROM DeviceEntity d

	    LEFT JOIN DeviceGroupMapping dgm
	        ON dgm.id = (
	            SELECT MAX(dgm2.id)
	            FROM DeviceGroupMapping dgm2
	            WHERE dgm2.device = d
	        )

	    LEFT JOIN GroupEntity g ON g = dgm.group
	    LEFT JOIN Users u ON u.id = dgm.userId

	    WHERE d.id = :deviceId
	""")
	Optional<DevicesUpdateDto> findDeviceForAdmin(@Param("deviceId") Long deviceId);

	@Query("""
			SELECT new com.trackingpath.dtos.DevicesUpdateDto(
			    d.id,
			    d.devicetimezone,
			    d.name,
			    d.deviceModel,
			    d.objectIcon,
			    d.uniqueid,
			    d.odometer,
			    d.vehicleStatus,

			    g.id,
			    g.name,

			    CONCAT(u.username,'[ ',u.firstname,' ',u.lastname,' ]'),
			    u.id,
			    u.accountname,

			    d.simCardNumber,
			    d.simActivationDate,
			    d.simExpirationDate,

			    d.vin,
			    d.installationDate,
			    d.plateNumber,
			    d.registrationNumber,
			    d.owner,

			    d.fuelMeasureName,
			    d.fuelMeasurement,
			    d.fuelCost,

			    d.iconType,
			    d.movingIconColor,
			    d.stoppedIconColor,
			    d.offlineIconColor,
			    d.engineIdleColor,

			    d.tailColor,
			    d.tailLength,
			    d.imgIconName,
			    d.imgIconType,

			    d.maxSpeed,
			    d.minMovingSpeed,
			    d.minFuelFillings,
			    d.minFuelTheft,
			    d.fuelChangeAfterStop,
			    d.rcPath,
                d.insurancePath
			)
			FROM DeviceEntity d
			JOIN DeviceGroupMapping dgm 
			    ON dgm.device = d AND dgm.userId = :userId
			JOIN GroupEntity g ON g = dgm.group
			JOIN Users u ON u.id = d.userId
			WHERE d.id = :deviceId
			""")
			Optional<DevicesUpdateDto> findDeviceForUser(
			    @Param("deviceId") Long deviceId,
			    @Param("userId") Long userId
			);
	
	@Query("SELECT d.status FROM DeviceEntity d WHERE d.id = :deviceId")
    String findStatusByDeviceId(@Param("deviceId") Long deviceId);

	
	@Query("""
		    SELECT new com.trackingpath.dtos.SetupDeviceDTO(
		        d.id,
		        d.vehicleStatus,
		        d.name,
		        d.uniqueid
		    )
		    FROM DeviceEntity d
		    WHERE
		        (
		            :isAdmin = true
		            OR
		            (:isAdmin = false AND d.id IN :deviceIds)
		        )
		    AND (
		        :search = '%%'
		        OR LOWER(d.name) LIKE LOWER(:search)
		        OR LOWER(d.uniqueid) LIKE LOWER(:search)
		        OR LOWER(d.vehicleStatus) LIKE LOWER(:search)
		    )
		    ORDER BY d.id DESC
		""")
		Page<SetupDeviceDTO> findObjectData(
		        @Param("isAdmin") boolean isAdmin,
		        @Param("deviceIds") List<Long> deviceIds,
		        @Param("search") String search,
		        Pageable pageable
		);


	    @Query(value = """
	        SELECT 
	            d.icon_type AS iconType,
	            d.img_icon_name AS imgIconName,
	            d.name AS name,d.devicetimezone as devicetimezone,
	            d.status AS status,
	            EXTRACT(EPOCH FROM l.lastidletime) AS lastidletime,
	            EXTRACT(EPOCH FROM l.lastmovementtime) AS lastmovementtime
	        FROM devices d
	        LEFT JOIN livedata l ON d.id = l.deviceid
	        WHERE d.id = :deviceId
	        """, nativeQuery = true)
	    Optional<DeviceInfoProjection> findDeviceInfo(@Param("deviceId") Long deviceId);

	    @Query("SELECT d.id FROM DeviceEntity d WHERE d.userId = :userId")
	    List<Long> findDeviceIdsByUser(@Param("userId") Long userId);	

	    @Query("SELECT d.name FROM DeviceEntity d WHERE d.id IN :ids")
	    List<String> findDeviceNamesByIds(@Param("ids") List<Long> ids);

	    @Query("SELECT d.name, d.devicetimezone FROM DeviceEntity d WHERE d.id IN :ids")
	    List<Object[]> findDeviceDetailsByIds(@Param("ids") List<Long> ids);
		
	    @Query("SELECT d.id, d.devicetimezone FROM DeviceEntity d WHERE d.id IN :ids")
	    List<Object[]> findDeviceTimeZoneByIds(@Param("ids") List<Long> ids);
	    @Query("SELECT d.devicetimezone FROM DeviceEntity d WHERE d.id = :deviceId")
	    String findDeviceTimeZone(@Param("deviceId") Long deviceId);
		
	    
	    
	    @Query(value = """
	    		SELECT 
	    		    l.attributes,
	    		    l.devicetime,
	    		    l.speed,
                    l.latitude,
                    l.longitude,
	    		    CASE 
	    		        WHEN d.devicetimezone IS NULL 
	    		             OR d.devicetimezone IN ('UTC', 'Etc/UTC')
	    		        THEN l.devicetime
	    		        ELSE 
	    		            (l.devicetime AT TIME ZONE d.devicetimezone) AT TIME ZONE 'UTC'
	    		    END as utc_time

	    		FROM devices d
	    		JOIN eventdata l ON d.id = l.deviceid

	    		WHERE l.deviceid = :deviceId
	    		  AND (
	    		        CASE 
	    		            WHEN d.devicetimezone IS NULL 
	    		                 OR d.devicetimezone IN ('UTC', 'Etc/UTC')
	    		            THEN l.devicetime
	    		            ELSE 
	    		                (l.devicetime AT TIME ZONE d.devicetimezone) AT TIME ZONE 'UTC'
	    		        END
	    		      ) BETWEEN :stime AND :etime

	    		ORDER BY utc_time ASC
	    		""", nativeQuery = true)
	    		List<Object[]> getTodayactivity(Long deviceId, LocalDateTime stime, LocalDateTime etime);

	    	@Query("SELECT d.devicetimezone FROM DeviceEntity d WHERE d.id = :deviceId")
	    	String findTimezoneByDeviceId(@Param("deviceId") Long deviceId);
}
