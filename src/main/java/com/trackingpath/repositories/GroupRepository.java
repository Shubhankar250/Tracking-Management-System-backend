package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.GroupEntity;

import jakarta.transaction.Transactional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

	@Query("""
			    SELECT g
			    FROM GroupEntity g
			    WHERE g.user.id = :userId
			    ORDER BY g.id ASC
			""")
	List<GroupEntity> findByAdminUser(@Param("userId") Long userId);

	@Query("""
			    SELECT DISTINCT g
			    FROM DeviceGroupMapping dgm
			    JOIN dgm.group g
			    JOIN dgm.device d
			    WHERE d.id IN :deviceIds
			    ORDER BY g.id ASC
			""")
	List<GroupEntity> findGroupsByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

	@Modifying
	@Transactional
	@Query("""
	        DELETE FROM DeviceGroupMapping dgm
	        WHERE dgm.group.id = :groupId
	          AND dgm.userId = :userId
	       """)
	int deletePreviousGroupDevices(
	        @Param("groupId") Long groupId,
	        @Param("userId") Long userId
	);


    @Modifying
    @Transactional
    @Query("""
            UPDATE GroupEntity g
            SET g.name = :groupName
            WHERE g.id = :groupId
              AND g.user.id = :userId
           """)
    int updateGroupName(
            @Param("groupName") String groupName,
            @Param("groupId") Long groupId,
            @Param("userId") Long userId
    );

    Optional<GroupEntity> findByNameAndAdminId(String name, Long adminId);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.id = 1")
    Optional<GroupEntity> findDefaultGroup();

}
