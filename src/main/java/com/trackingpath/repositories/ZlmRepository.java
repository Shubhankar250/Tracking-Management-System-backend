package com.trackingpath.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.ExternalAccessToken;

import jakarta.transaction.Transactional;

@Repository
public interface ZlmRepository  extends JpaRepository<ExternalAccessToken, Long> {

	Optional<ExternalAccessToken> findByProjectName(String projectDescription);

	@Query("""
		       SELECT e FROM ExternalAccessToken e
		       WHERE LOWER(e.username) LIKE LOWER(CONCAT('%', :search, '%'))
		       OR LOWER(e.projectName) LIKE LOWER(CONCAT('%', :search, '%'))
		       OR LOWER(e.url) LIKE LOWER(CONCAT('%', :search, '%'))
		       """)
		Page<ExternalAccessToken> searchTokens(
		        @Param("search") String search,
		        Pageable pageable);
	

	 @Modifying
	    @Transactional
	    @Query("UPDATE ExternalAccessToken e " +
	           "SET e.externalAccessToken = :token " +
	           "WHERE e.projectName = :projectName")
	    int updateTokenByProjectName(@Param("projectName") String projectName,
	                                 @Param("token") String token);








}


