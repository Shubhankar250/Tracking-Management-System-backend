package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.entities.PoiGroup;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PoiGroupRepository extends JpaRepository<PoiGroup, Integer> {

    List<PoiGroup> findByAdminId(Long adminId);

    // ✅ Delete with admin check (same pattern as route)
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM PoiGroup p
        WHERE p.id = :id
          AND p.adminId = :adminId
    """)
    int deleteByIdAndAdminId(@Param("id") Integer id,
                             @Param("adminId") Long adminId);
}