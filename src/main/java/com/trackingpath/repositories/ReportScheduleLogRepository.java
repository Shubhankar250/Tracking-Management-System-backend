package com.trackingpath.repositories;

import com.trackingpath.dtos.ReportScheduleLogDto;
import com.trackingpath.entities.ReportScheduleLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportScheduleLogRepository extends JpaRepository<ReportScheduleLog, Long> {
	@Query("""
		    SELECT new com.trackingpath.dtos.ReportScheduleLogDto(
		        l.id,
		        l.scheduleId,
		        s.title,
		        s.reportType,
		        s.outputFormat,
		        l.startedAt,
		        l.completedAt,
		        l.status,
		        l.recipientCount,
		        l.filePath,
		        l.errorMessage,
		        l.fileSize,	
		        s.daily,
		        s.weekly,
		        s.monthly,
		        'GENERATED'  
		       
		    )
		    FROM ReportScheduleLog l
		    JOIN ReportSchedule s ON l.scheduleId = s.id
		    WHERE s.userId = :userId
		    AND (
		        :search = '' OR
		        LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
		        LOWER(s.reportType) LIKE LOWER(CONCAT('%', :search, '%'))OR
		        LOWER(s.outputFormat) LIKE LOWER(CONCAT('%', :search, '%'))
		        
		    )
		    ORDER BY l.completedAt DESC
		""")
		Page<ReportScheduleLogDto> getScheduleLogs(
		        @Param("userId") Long userId,
		        @Param("search") String search,
		        Pageable pageable
		);
}
