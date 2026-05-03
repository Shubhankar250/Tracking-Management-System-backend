package com.trackingpath.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.trackingpath.entities.ParentChildMap;

@Repository
public interface ParentChildMapRepository extends JpaRepository<ParentChildMap, Long> {

    @Query("select m.parentId from ParentChildMap m where m.loginPassengerId = :loginPassengerId and m.active = true")
    Long findAnyParentIdByLoginPassengerId(@Param("loginPassengerId") Long loginPassengerId);

    List<ParentChildMap> findByParentIdAndActiveTrue(Long parentId);

    Optional<ParentChildMap> findByParentIdAndChildPassengerIdAndActiveTrue(Long parentId, Long childPassengerId);
}
