package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.ReportLogEntity;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLogEntity, Long> {
	
	 @Query(
		        value = """
		            SELECT r
		            FROM ReportLogEntity r
		            WHERE (
		                :search = '' OR
		                LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.type) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.format) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.dateFrom) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.dateTo) LIKE LOWER(CONCAT('%', :search, '%'))
		            )
		        """,
		        countQuery = """
		            SELECT COUNT(r)
		            FROM ReportLogEntity r
		            WHERE (
		                :search = '' OR
		                LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.type) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.format) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.dateFrom) LIKE LOWER(CONCAT('%', :search, '%')) OR
		                LOWER(r.dateTo) LIKE LOWER(CONCAT('%', :search, '%'))
		            )
		        """
		    )
		    Page<ReportLogEntity> findAll(
		        @Param("search") String search,
		        Pageable pageable
		    );
	
}