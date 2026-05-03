package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.SharePositionBean;
import com.trackingpath.entities.ApiManagerEntity;

@Repository
public interface ApiManagerRepository extends JpaRepository<ApiManagerEntity, Long> {

	@Query("""
		    SELECT s FROM ApiManagerEntity s
		    WHERE s.userId = :userId
		    AND (
		        :search = '' OR
		        LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
		        OR LOWER(s.baseUrl) LIKE LOWER(CONCAT('%', :search, '%'))
		        OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
		        OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%'))
		    )
		""")
		Page<ApiManagerEntity> findByUserIdWithSearch(
		        @Param("userId") Long userId,
		        @Param("search") String search,
		        Pageable pageable
		);

    List<ApiManagerEntity> findByAdminId(Long adminId);

    Optional<List<ApiManagerEntity>> findByAccessCode(Long accessCode);


    @Query(value = """
            SELECT a.id,
                   d.name AS device_name,
                   a.access_start_time,
                   a.access_end_time,
                   a.device_id,
                   a.access_code,
                   a.valid_time,
                   a.status,
                   a.url AS baseurl
            FROM api_manager a
            JOIN devices d ON d.id::text = ANY(string_to_array(a.device_id, ','))
            WHERE a.access_code = :accessCode
        """, nativeQuery = true)
    List<SharePositionBean> findActiveShares(@Param("accessCode") Long accessCode);
}
