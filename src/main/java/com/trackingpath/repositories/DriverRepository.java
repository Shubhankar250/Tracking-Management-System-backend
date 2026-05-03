package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.DriverSetupBean;
import com.trackingpath.dtos.DriverView;
import com.trackingpath.entities.Driveres;


@Repository
public interface DriverRepository extends JpaRepository<Driveres, Long> {
    List<Driveres> findByDeviceId(Long deviceId);
    
    
    List<DriverView> findByDevice_Id(Long deviceId);
    Optional<Driveres> findDriverByUsername(String username);
    
    @Query("""
            SELECT new com.trackingpath.dtos.DriverSetupBean(
                d.id,
                d.name,
                d.device.id,
                d.device.name,
                d.currentDevice.id,
                d.rfid,
                d.phone,
                d.email,
                d.description,
                d.username
            )
            FROM Driveres d
            WHERE d.userId = :userId
            AND (
                LOWER(d.name) LIKE LOWER(:search) 
                OR LOWER(d.device.name) LIKE LOWER(:search)
                OR LOWER(d.rfid) LIKE LOWER(:search)
                OR LOWER(d.phone) LIKE LOWER(:search)
                OR LOWER(d.email) LIKE LOWER(:search)
                OR LOWER(d.description) LIKE LOWER(:search)
            )
        """)
        Page<DriverSetupBean> findDriversByUser(
            @Param("userId") Long userId,
            @Param("search") String search,
            Pageable pageable
        );

        // Pagination + search for admin
        @Query("""
            SELECT new com.trackingpath.dtos.DriverSetupBean(
                d.id,
                d.name,
                d.device.id,
                d.device.name,
                d.currentDevice.id,
                d.rfid,
                d.phone,
                d.email,
                d.description,
                d.username
                  
                
            )
            FROM Driveres d
            WHERE d.adminId = :adminId
            AND (
                LOWER(d.name) LIKE LOWER(:search) 
                OR LOWER(d.device.name) LIKE LOWER(:search)
                OR LOWER(d.rfid) LIKE LOWER(:search)
                OR LOWER(d.phone) LIKE LOWER(:search)
                OR LOWER(d.email) LIKE LOWER(:search)
                OR LOWER(d.description) LIKE LOWER(:search)
            )
        """)
        Page<DriverSetupBean> findDriversByAdmin(
            @Param("adminId") Long adminId,
            @Param("search") String search,
            Pageable pageable
        );


	
		@Query(" SELECT new com.trackingpath.entities.Driveres(d.id, d.name) FROM Driveres d")
	    List<Driveres> findAllDriver();
		
		@Query("SELECT d.id, d.name, d.username, dev.name " +
			       "FROM Driveres d LEFT JOIN d.device dev")
			List<Object[]> findDrivers();
			
			@Query("SELECT d.device.id FROM Driveres d WHERE d.device IS NOT NULL")
	    	List<Long> findAssignedDeviceIds();
}
