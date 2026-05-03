package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.entities.Users;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<Users, Long> {

    @EntityGraph(attributePaths = "roles")
    Users findByUsername(String username);
    

    @EntityGraph(attributePaths = "roles")
    Optional<Users> findWithRolesById(Long id);


	@EntityGraph(attributePaths = "roles")
	List findAll();
	
	@EntityGraph(attributePaths = "roles")
    List<Users> findByAdminId(Long adminId);
	
	@EntityGraph(attributePaths = "roles")
	boolean existsByIdAndRolesRoleName(Long userId, String roleName);
	
	
	@Query("""
		    SELECT u FROM Users u
		    WHERE 
		        (:adminId IS NULL OR u.adminId = :adminId)
		        AND NOT EXISTS (
		            SELECT r FROM u.roles r 
		            WHERE r.roleName = 'ROLE_ADMIN'
		        )
		        AND (
		        :search = '' OR
	               LOWER(u.username) LIKE CONCAT('%', LOWER(:search), '%')
	               OR LOWER(u.firstname) LIKE CONCAT('%', LOWER(:search), '%')
	               OR LOWER(u.lastname) LIKE CONCAT('%', LOWER(:search), '%')
	               OR LOWER(u.phone_number1) LIKE CONCAT('%', LOWER(:search), '%')
	            )
		""")
		Page<Users> findUsersExcludingAdmins(
		        @Param("adminId") Long adminId,
		        @Param("search") String search,
		        Pageable pageable
		);



	 @Modifying
   @Transactional
   @Query(value = "UPDATE users SET resetpasswordtoken = :token, resetpasswordexpires = now() + interval '5 minutes' WHERE username = :username", nativeQuery = true)
   int updateResetToken(@Param("username") String username, @Param("token") String token);

 // ===== NEW PASSWORD RESET =====
   @Modifying
   @Transactional
   @Query(value = "UPDATE users SET password = :newPassword WHERE resetpasswordtoken = :token", nativeQuery = true)
   int newPasswordReset(@Param("newPassword") String newPassword, @Param("token") String token);

   // ===== DISABLE TOKEN AFTER USE =====
   @Modifying
   @Transactional
   @Query(value = "UPDATE users SET resetpasswordtoken = NULL WHERE resetpasswordtoken = :token", nativeQuery = true)
   int updateDisableStatus(@Param("token") String token);


   @Query(value = "SELECT count(*) > 0 FROM users WHERE resetpasswordtoken = :token AND resetpasswordexpires >= now()", nativeQuery = true)
   boolean getEnableStatus(@Param("token") String token);

 
   
   
   @Query(value = """
		    SELECT 
		        COALESCE(g.name, 'Ungrouped') AS group_name,
		        d.id,
		        d.name
		    FROM devices d
		    LEFT JOIN (
		        SELECT DISTINCT ON (device_id)
		            device_id, group_id, user_id
		        FROM devices_group_mapping
		        ORDER BY device_id, creation_time DESC
		    ) dgm ON dgm.device_id = d.id
		    LEFT JOIN groups g ON g.id = dgm.group_id
		    WHERE (
		        :isAdmin = true
		        OR (
		            d.user_id = :userId
		            OR d.id IN (:deviceIds)
		        )
		    )
		    ORDER BY group_name, d.name
		""", nativeQuery = true)
		List<Object[]> fetchDevicesWithGroup(Long userId, boolean isAdmin, List<Long> deviceIds);


		@Query("""
			    SELECT new com.trackingpath.dtos.CustomUserDTO(
			        u.smsGatewayType,
			        u.smsGatewayUrl,
			        u.smtpHost,
			        u.smtpUsername,
			        u.smtpPassword,
			        u.smtpPort,
			        u.smtpEncryption,
			        u.availableWidgets,
			        u.dashboardMenu,
			        u.availablesubscriptionpoints,
			        u.timezone
			    )
			    FROM Users u
			    WHERE u.id = :userId
			""")
			CustomUserDTO getUserData(@Param("userId") Long userId);
		
		@Query("""
				SELECT u, ug.groupId
				FROM Users u
				LEFT JOIN UserGroupMapping ug ON ug.userId = u.id
				LEFT JOIN FETCH u.roles
				WHERE u.id = :id
				""")
				List<Object[]> findUserWithRolesAndGroup(@Param("id") Long id);
				
				@Query("SELECT u FROM Users u WHERE u.username <> :username")
				List<Users> findAllExceptCurrentUser(@Param("username") String username);
				
				

				
				@Query("SELECT u.id, u.firstname, u.username, u.accountname, r.roleName " +
					       "FROM Users u LEFT JOIN Role r ON r.user.id = u.id " +
					       "WHERE u.accountname = :accountname " +
					       "AND u.username <> :username")
					List<Object[]> findUsersByAccount(@Param("accountname") String accountname,
					                                  @Param("username") String username);
					
					@Modifying
					@Transactional
					@Query("UPDATE Users u SET u.loggedIn = CURRENT_TIMESTAMP WHERE u.username = :username")
					int updateLoggedInByUsername(@Param("username") String username);
					@Modifying
					@Transactional
					@Query("UPDATE Users u SET u.loggedOut = CURRENT_TIMESTAMP WHERE u.username = :username")
					int updateLoggedOutByUsername(@Param("username") String username);

}