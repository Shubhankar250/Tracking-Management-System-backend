package com.trackingpath.repositories;

import com.trackingpath.dtos.ReportScheduleDto;
import com.trackingpath.entities.ReportSchedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    Optional<ReportSchedule> findByIdAndActiveTrue(Long id);
    @Query(
    	    value = """
    	       SELECT new com.trackingpath.dtos.ReportScheduleDto(
    	            r.id,
    	            r.title,
    	            r.reportType,
    	            r.outputFormat,
    	            r.createdAt,
    	            r.active,
    	            r.daily,
    	            r.weekly,
    	            r.monthly
    	       )
    	       FROM ReportSchedule r
    	       WHERE r.userId = :userId
    	       AND r.active = true
    	       AND (
    	            :search = '' OR
    	            LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
    	            LOWER(r.reportType) LIKE LOWER(CONCAT('%', :search, '%')) OR
    	            LOWER(r.outputFormat) LIKE LOWER(CONCAT('%', :search, '%'))
    	       )
    	       ORDER BY r.createdAt DESC
    	    """,
    	    countQuery = """
    	       SELECT COUNT(r)
    	       FROM ReportSchedule r
    	       WHERE r.userId = :userId
    	       AND r.active = true
    	       AND (
    	            :search = '' OR
    	            LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
    	            LOWER(r.reportType) LIKE LOWER(CONCAT('%', :search, '%')) OR
    	            LOWER(r.outputFormat) LIKE LOWER(CONCAT('%', :search, '%'))
    	       )
    	    """
    	)
    	Page<ReportScheduleDto> getScheduleData(
    	        @Param("userId") Long userId,
    	        @Param("search") String search,
    	        Pageable pageable
    	);
}
