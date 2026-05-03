package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.trackingpath.dtos.AlertDataDTO;
import com.trackingpath.entities.Alert;
import com.trackingpath.entities.Users;

import jakarta.transaction.Transactional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
	
	@Query(
		    value = """
		        SELECT new com.trackingpath.dtos.AlertDataDTO(
		            a.id,
		            a.alertName,
		            ad.alertType,
		            a.status,
		            COUNT(adm.id)
		        )
		        FROM Alert a
		        LEFT JOIN AlertDetails ad ON ad.alert.id = a.id
		        LEFT JOIN AlertDeviceMapping adm ON adm.alert.id = a.id
		        WHERE a.adminId = :adminId
		          AND (
		                :search = '' OR
		                LOWER(a.alertName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(ad.alertType) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(a.status) LIKE LOWER(CONCAT('%', :search, '%'))
		              )
		        GROUP BY a.id, a.alertName, ad.alertType, a.status
		    """,
		    countQuery = """
		        SELECT COUNT(DISTINCT a.id)
		        FROM Alert a
		        LEFT JOIN AlertDetails ad ON ad.alert.id = a.id
		        WHERE a.adminId = :adminId
		          AND (
		                :search = '' OR
		                LOWER(a.alertName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(ad.alertType) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(a.status) LIKE LOWER(CONCAT('%', :search, '%'))
		              )
		    """
		)
		Page<AlertDataDTO> findAlertsDataForAdmin(
		        @Param("adminId") Long adminId,
		        @Param("search") String search,
		        Pageable pageable
		);
	
	@Query(
		    value = """
		        SELECT new com.trackingpath.dtos.AlertDataDTO(
		            a.id,
		            a.alertName,
		            ad.alertType,
		            a.status,
		            COUNT(adm.id)
		        )
		        FROM Alert a
		        LEFT JOIN AlertDetails ad ON ad.alert.id = a.id
		        LEFT JOIN AlertDeviceMapping adm ON adm.alert.id = a.id
		        WHERE a.adminId = :adminId
		          AND a.userId = :userId
		          AND (
		                :search = '' OR
		                LOWER(a.alertName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(ad.alertType) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(a.status) LIKE LOWER(CONCAT('%', :search, '%'))
		              )
		        GROUP BY a.id, a.alertName, ad.alertType, a.status
		    """,
		    countQuery = """
		        SELECT COUNT(DISTINCT a.id)
		        FROM Alert a
		        LEFT JOIN AlertDetails ad ON ad.alert.id = a.id
		        WHERE a.adminId = :adminId
		          AND a.userId = :userId
		          AND (
		                :search = '' OR
		                LOWER(a.alertName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(ad.alertType) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(a.status) LIKE LOWER(CONCAT('%', :search, '%'))
		              )
		    """
		)
		Page<AlertDataDTO> findAlertsDataForUser(
		        @Param("adminId") Long adminId,
		        @Param("userId") Long userId,
		        @Param("search") String search,
		        Pageable pageable
		);


	@Modifying
	@Query("UPDATE Alert a SET a.status = CASE WHEN a.status = 'ACTIVE' THEN 'INACTIVE' ELSE 'ACTIVE' END WHERE a.id = :id AND a.adminId = :adminId")
	int toggleStatus(@Param("id") Long id, @Param("adminId") Long adminId);



	@Modifying
    @Transactional
    @Query("UPDATE Alert a SET a.alertName = :name WHERE a.id = :id AND a.adminId = :adminId")
    int forceUpdateAlertName(@Param("id") Long id,
                                                     @Param("name") String name,
                                                     @Param("adminId") Long adminId);



	
}
