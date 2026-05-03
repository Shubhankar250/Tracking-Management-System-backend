package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.CommandEntity;
import com.trackingpath.entities.Users;


public interface CommandRepository extends JpaRepository<CommandEntity, Long> {

	 @Query("SELECT DISTINCT c.model FROM CommandEntity c")
	    List<String> findDistinctModels();
	 @Query(value = """
		        SELECT DISTINCT c.command_code, c.command_name
		        FROM command_data c
		        INNER JOIN devices d ON d.device_model = c.model
		        WHERE d.id = :deviceId
		        """, nativeQuery = true)
		    List<Object[]> findCommandsByDeviceId(@Param("deviceId") long deviceId);

		    @Query(value = """
		        SELECT DISTINCT c.command_code, c.command_name
		        FROM command_data c
		        INNER JOIN devices d ON d.device_model = c.model
		        WHERE d.id = :deviceId
		          AND c.command_status = 1
		        """, nativeQuery = true)
		    List<Object[]> findActiveCommandsByDeviceId(@Param("deviceId") long deviceId);
		    
		    @Query("""
		    	    SELECT c FROM CommandEntity c
		    	    WHERE
		    	        ( 
		    	            :search IS NULL OR :search = ''  OR
		    	            LOWER(c.model) LIKE LOWER(CONCAT('%', :search, '%')) OR
		    	            LOWER(c.commandName) LIKE LOWER(CONCAT('%', :search, '%')) OR
		    	            LOWER(c.commandCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
		    	            LOWER(c.types) LIKE LOWER(CONCAT('%', :search, '%'))
		    	        )
		    	    ORDER BY c.id DESC
		    	""")
		    	Page<CommandEntity> findAllCommands(
		    	        @Param("search") String search,
		    	        Pageable pageable
		    	);
		    
		    @Query(value = """
		            SELECT DISTINCT c.command_name
		            FROM command_data c
		            WHERE c.command_status = 1
		              AND c.types LIKE CONCAT('%', :alertType, '%')
		            """, nativeQuery = true)
		    List<String> getCommandName(@Param("alertType") String alertType);
		    
}