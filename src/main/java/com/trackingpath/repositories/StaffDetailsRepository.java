package com.trackingpath.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.StaffDetails;

public interface StaffDetailsRepository extends JpaRepository<StaffDetails, Long> {
	@Query("""
		    SELECT s FROM StaffDetails s
		    WHERE LOWER(s.name) LIKE LOWER(:search)
		       OR LOWER(s.email) LIKE LOWER(:search)
		       OR LOWER(s.employeeCode) LIKE LOWER(:search)
		       OR LOWER(s.mobileNumber) LIKE LOWER(:search)
		       OR LOWER(s.designation) LIKE LOWER(:search)
		""")
		Page<StaffDetails> findBySearch(
		        @Param("search") String search,
		        Pageable pageable
		);
}
