package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.dtos.DeviceGroupDataProjection;
import com.trackingpath.dtos.DeviceModalDTO;
import com.trackingpath.dtos.DeviceModalSearchDTO;
import com.trackingpath.entities.DeviceModalEntity;
import com.trackingpath.entities.Users;

public interface DeviceModalRepository extends JpaRepository<DeviceModalEntity, Long> {

	@Query("""
		    SELECT new com.trackingpath.dtos.DeviceModalDTO(
		        d.id,
		        d.companyName,
		        d.modalName,
		        d.modalType,
		        d.noOfChannel,
		        d.image,
		        d.userManual,
		        d.protocolManual,
		        d.commands,
		        d.connectedIP,
		        d.connectedPort,
		        d.noOfDIN,
		        d.noOfAIN,
		        d.noOfDOUT,
		        d.protocolName,
		        d.adasAlertType,
                d.dmsAlertType,
                d.active
		    )
		    FROM DeviceModalEntity d
		    WHERE 
		        (:isAdmin = true AND d.adminId = :adminId
		         OR :isAdmin = false AND d.userId = :userId)
		    AND (
		        LOWER(d.modalName) LIKE LOWER(:search)
		        OR LOWER(d.modalType) LIKE LOWER(:search)
		        OR LOWER(d.connectedIP) LIKE LOWER(:search)
		        OR LOWER(d.connectedPort) LIKE LOWER(:search)
		    )
		""")
		Page<DeviceModalDTO> findDeviceModalsWithSearch(
		        @Param("adminId") Long adminId,
		        @Param("userId") Users userId,
		        @Param("isAdmin") boolean isAdmin,
		        @Param("search") String search,
		        Pageable pageable
		);
	@Query("SELECT d.modalName FROM DeviceModalEntity d WHERE d.active = true")
	List<String> findAllModalNames();

    @Query("""
    	    SELECT new com.trackingpath.dtos.DeviceModalSearchDTO(
    	        d.id,
    	        d.modalName,
    	        d.image
    	    )
    	    FROM DeviceModalEntity d
    	    WHERE 
    	        (d.active = true)
    	        AND LOWER(d.modalName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    	    ORDER BY d.modalName
    	""")
    	List<DeviceModalSearchDTO> searchDeviceModal(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT d.companyName FROM DeviceModalEntity d WHERE d.companyName IS NOT NULL ORDER BY d.companyName ASC")
    List<String> findAllCompanyNames();
    
    @Query("""
    	       SELECT new com.trackingpath.dtos.DeviceModalDTO(
    	               d.id,
    	               d.companyName,
    	               d.modalName,
    	               d.modalType,
    	               d.noOfChannel,
    	               d.image,
    	               d.userManual,
    	               d.protocolManual,
    	               d.commands,
    	               d.connectedIP,
    	               d.connectedPort,
    	               d.noOfDIN,
    	               d.noOfAIN,
    	               d.noOfDOUT,
    	               d.protocolName,
    	               d.adasAlertType,
    	               d.dmsAlertType,
    	               d.active
    	       )
    	       FROM DeviceModalEntity d
    	       WHERE 
    	           (d.active = true)
    	           AND (
    	               :companyName IS NULL 
    	               OR :companyName = '' 
    	               OR LOWER(d.companyName) = LOWER(:companyName)
    	           )
    	""")
    	List<DeviceModalDTO> findAllByCompanyName(@Param("companyName") String companyName);

    @Query(value = """
            SELECT 
            d.id AS deviceId,
            d.name AS deviceName,
            dm.modal_type AS modalType,
            CASE 
                   WHEN dm.modal_type IN ('DashCam','Gps+DashCam') 
                   THEN dm.adas_alert_type 
                   ELSE NULL 
            END AS adasAlertType,
            CASE 
                   WHEN dm.modal_type IN ('DashCam','Gps+DashCam') 
                   THEN dm.dms_alert_type 
                   ELSE NULL 
            END AS dmsAlertType
            FROM devices d
            LEFT JOIN device_modal dm 
                         ON d.device_model = dm.modal_name
            WHERE d.id IN (:deviceIds)
            """, nativeQuery = true)
            List<DeviceGroupDataProjection> getGroupedDevices(@Param("deviceIds") List<Long> deviceIds);
	
}
