package com.trackingpath.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.ReportDTO;
import com.trackingpath.entities.ReportEntity;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
	@Query(
		    value = """
		        SELECT new com.trackingpath.dtos.ReportDTO(
		            r.id,
		            r.title,
		            r.type,
		            r.format,
		            r.period
		        )
		        FROM ReportEntity r
		        WHERE (
		            :search = '' OR
		            LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.type) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.format) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.period) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		    """,
		    countQuery = """
		        SELECT COUNT(r)
		        FROM ReportEntity r
		        WHERE (
		            :search = '' OR
		            LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.type) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.format) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(r.period) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		    """
		)
		Page<ReportDTO> getReportData(
		        @Param("search") String search,
		        Pageable pageable
		);
	
	Optional<ReportEntity> findByIdAndAdminId(Long id, Long adminId);
}