package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.SubscriptionMasterEntity;

import jakarta.transaction.Transactional;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<SubscriptionMasterEntity, Long> {

    // ✅ Used by getSubscriptions()
	@Query(
		    value = """
		        SELECT sm
		        FROM SubscriptionMasterEntity sm
		        JOIN FETCH sm.country c
		        WHERE (:country IS NULL OR c.countryName = :country)
		        AND (
		            :search IS NULL OR :search = '' OR
		            LOWER(sm.subDetails) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(c.countryName) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		    """,
		    countQuery = """
		        SELECT COUNT(sm)
		        FROM SubscriptionMasterEntity sm
		        JOIN sm.country c
		        WHERE (:country IS NULL OR c.countryName = :country)
		        AND (
		            :search IS NULL OR :search = '' OR
		            LOWER(sm.subDetails) LIKE LOWER(CONCAT('%', :search, '%')) OR
		            LOWER(c.countryName) LIKE LOWER(CONCAT('%', :search, '%'))
		        )
		    """
		)
		Page<SubscriptionMasterEntity> findAllByCountry(
		        @Param("country") String country,
		        @Param("search") String search,
		        Pageable pageable
		);

	
    // ✅ Used by getById()
    @Query("""
        SELECT sm
        FROM SubscriptionMasterEntity sm
        JOIN FETCH sm.country
        WHERE sm.id = :id
    """)
    Optional<SubscriptionMasterEntity> findByIdWithCountry(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET available_subscription_points = COALESCE(available_subscription_points, 0) + :points WHERE id = :userId", nativeQuery = true)
    int updateSubscriptionPoints(@Param("userId") long userId, @Param("points") int points);
}

