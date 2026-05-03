package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.dtos.DeviceGroupDataDto;
import com.trackingpath.dtos.DevicesUpdateDto;
import com.trackingpath.entities.DeviceGroupMapping;

public interface DeviceGroupMappingRepository extends JpaRepository<DeviceGroupMapping, Long> {
	
    List<DeviceGroupMapping> findByDevice_UserId(Long userId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM DeviceGroupMapping dgm
        WHERE dgm.group.name = 'Ungrouped'
          AND dgm.device.id IN :deviceIds
    """)
    int deleteUngroupedDevices(@Param("deviceIds") List<Long> deviceIds);


    @Modifying
    @Transactional
    @Query("""
        DELETE FROM DeviceGroupMapping dgm
        WHERE dgm.device.id IN :deviceIds
        AND dgm.group.id <> :groupId
    """)
    void deleteDevicesFromOtherGroups(@Param("deviceIds") List<Long> deviceIds,
                                      @Param("groupId") Long groupId);


    @Query("""
    	    SELECT new com.trackingpath.dtos.DeviceGroupDataDto(
    	        d.id,
    	        d.name,
    	        g.id,
    	        g.name
    	    )
    	    FROM DeviceGroupMapping dgm
    	    JOIN dgm.device d
    	    JOIN dgm.group g
    	    WHERE g.name = :groupName
    	      AND d.id IN :deviceIds
    	      AND dgm.adminId = :adminId
    	""")
    	List<DeviceGroupDataDto> getDeviceGroupDataForUpdate(
    	        @Param("groupName") String groupName,
    	        @Param("deviceIds") List<Long> deviceIds,
    	        @Param("adminId") Long adminId
    	);


    @Modifying
    @Transactional
    @Query("""
        DELETE FROM GroupEntity g
        WHERE g.id NOT IN (
            SELECT DISTINCT dgm.group.id FROM DeviceGroupMapping dgm
        )
        AND g.name <> 'Ungrouped'
        AND g.user.id = :userId
    """)
    int deleteUnusedGroups(@Param("userId") Long userId);

    Optional<DeviceGroupMapping> findByDeviceId(Long deviceId);
    
    boolean existsByDevice_IdAndGroup_IdAndUserIdAndAdminId(
            Long deviceId,
            Long groupId,
            Long userId,
            Long adminId
    );

    @Query("""
    		SELECT dgm
    		FROM DeviceGroupMapping dgm
    		WHERE dgm.userId = :userId
    		AND dgm.adminId = :adminId
    		AND dgm.device.id IN :deviceIds
    		""")
    		List<DeviceGroupMapping> findMappings(
    		        Long userId,
    		        Long adminId,
    		        List<Long> deviceIds
    		);

    @Query("""
    	    SELECT dgm
    	    FROM DeviceGroupMapping dgm
    	    WHERE dgm.userId = :userId
    	    AND dgm.adminId = :adminId
    	""")
    	List<DeviceGroupMapping> findAllMappingsByUser(
    	        Long userId,
    	        Long adminId
    	);
    
    Optional<DeviceGroupMapping> findByDeviceIdAndUserId(Long deviceId, Long userId);



	




    

    
}