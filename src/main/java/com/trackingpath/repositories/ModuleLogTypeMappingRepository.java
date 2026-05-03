package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.ModuleLogTypeMapping;

public interface ModuleLogTypeMappingRepository
extends JpaRepository<ModuleLogTypeMapping, Long> {

@Query("SELECT m.id, m.module FROM ModuleLogTypeMapping m ORDER BY m.module")
List<Object[]> findAllModules();


@Query("""
        SELECT m.logTypes
        FROM ModuleLogTypeMapping m
        WHERE m.module = :module
        """)
 List<String> findLogTypesByModule(@Param("module") String module);
}