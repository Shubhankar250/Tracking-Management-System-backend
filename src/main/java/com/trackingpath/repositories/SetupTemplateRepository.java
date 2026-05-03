package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.dtos.SetupTemplateBean;
import com.trackingpath.entities.SetupTemplateEntity;

public interface SetupTemplateRepository extends JpaRepository<SetupTemplateEntity, Long> {
	@Query("""
			    SELECT new com.trackingpath.dtos.SetupTemplateBean(
			        t.id,
			        t.title,
			        t.adapted,
			        t.message,
			        t.subject,
			        t.category,
			        t.templateName
			    )
			    FROM SetupTemplateEntity t
			    WHERE t.category = 'SMS'
			    AND (
			        (:isAdmin = true AND t.adminId = :adminId)
			        OR
			        (:isAdmin = false AND t.userId = :userId)
			    )
			    AND (
			        :search = '%%'
			        OR LOWER(t.title) LIKE LOWER(:search)
			        OR LOWER(t.adapted) LIKE LOWER(:search)
			        OR LOWER(t.message) LIKE LOWER(:search)
			    )
			    ORDER BY t.id DESC
			""")
	Page<SetupTemplateBean> findSmsTemplates(@Param("isAdmin") boolean isAdmin, @Param("adminId") Long adminId,
			@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("""
			    SELECT new com.trackingpath.dtos.SetupTemplateBean(
			        t.id,
			        t.title,
			        t.adapted,
			        t.message,
			        t.subject,
			        t.category,
			        t.templateName
			    )
			    FROM SetupTemplateEntity t
			    WHERE t.category = 'EMAIL'
			    AND (
			        (:isAdmin = true AND t.adminId = :adminId)
			        OR
			        (:isAdmin = false AND t.userId = :userId)
			    )
			    AND (
		        :search = '%%'
		        OR LOWER(t.title) LIKE LOWER(:search)
		        OR LOWER(t.templateName) LIKE LOWER(:search)
		        OR LOWER(t.subject) LIKE LOWER(:search)
		        OR LOWER(t.message) LIKE LOWER(:search)
		    )
			    ORDER BY t.id DESC
			""")
	Page<SetupTemplateBean> findEmailTemplates(@Param("isAdmin") boolean isAdmin, @Param("adminId") Long adminId,
			@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("""
			    SELECT new com.trackingpath.dtos.SetupTemplateBean(
			        t.id,
			        t.title,
			        t.adapted,
			        t.message,
			        t.subject,
			        t.category,
			        t.templateName
			    )
			    FROM SetupTemplateEntity t
			    WHERE t.category = 'GPRS'
			    AND (
			        (:isAdmin = true AND t.adminId = :adminId)
			        OR
			        (:isAdmin = false AND t.userId = :userId)
			    )
			    AND (
			    :search = '%%'
			    OR LOWER(t.title) LIKE LOWER(:search)
			    OR LOWER(t.adapted) LIKE LOWER(:search)
			    OR LOWER(t.message) LIKE LOWER(:search)
			)
			    ORDER BY t.id DESC
			""")
	Page<SetupTemplateBean> findGprsTemplates(@Param("isAdmin") boolean isAdmin, @Param("adminId") Long adminId,
			@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	
	@Query("SELECT t.id, t.templateName FROM SetupTemplateEntity t " +
		       "WHERE t.userId = :userId AND t.category = 'EMAIL'")
		List<Object[]> findAllTemplateByUserId(@Param("userId") Long userId);
}
