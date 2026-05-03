package com.trackingpath.repositories;

import com.trackingpath.dtos.TaskDTO;
import com.trackingpath.entities.Task;

import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

	@Query(
		    value = """
		        SELECT t
		        FROM Task t
		        JOIN FETCH t.device d
		        WHERE
		            (:deviceId = 0 OR t.objectId = :deviceId)
		            AND (CAST(:startTime AS timestamp) IS NULL OR t.pickupStartTime >= :startTime)
		            AND (CAST(:endTime AS timestamp) IS NULL OR t.pickupEndTime <= :endTime)
		            AND (
		                :search = '' OR
		                LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.priority) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.status) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.pickupAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.deliveryAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
		            )
		    """,
		    countQuery = """
		        SELECT COUNT(t)
		        FROM Task t
		        JOIN t.device d
		        WHERE
		            (:deviceId = 0 OR t.objectId = :deviceId)
		            AND (CAST(:startTime AS timestamp) IS NULL OR t.pickupStartTime >= :startTime)
		            AND (CAST(:endTime AS timestamp) IS NULL OR t.pickupEndTime <= :endTime)
		            AND (
		                :search = '' OR
		                LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.priority) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.status) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.pickupAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(t.deliveryAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
		            )
		    """
		)
		Page<Task> getTaskData(
		    @Param("startTime") Timestamp startTime,
		    @Param("endTime") Timestamp endTime,
		    @Param("deviceId") long deviceId,
		    @Param("search") String search,
		    Pageable pageable
		);
	@Query("SELECT t FROM Task t LEFT JOIN FETCH t.device WHERE t.id = :id")
	Optional<Task> findByIdWithDevice(@Param("id") long id);
	
	@Query("SELECT t.id, t.name, t.username FROM Task t")
	List<Object[]> findTasks();
}
