package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.RouteGroup;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface RouteGroupRepository extends JpaRepository<RouteGroup, Integer> {

    List<RouteGroup> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM RouteGroup r
        WHERE r.id = :id
          AND r.userId = :userId
    """)
    int deleteByIdAndUserId(@Param("id") Integer id,
                           @Param("userId") Long userId);

	List<RouteGroup> findByAdminId(long adminId);
}
