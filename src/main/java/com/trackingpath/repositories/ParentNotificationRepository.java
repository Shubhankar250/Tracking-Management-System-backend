package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.ParentNotification;

@Repository
public interface ParentNotificationRepository extends JpaRepository<ParentNotification, Long> {
    List<ParentNotification> findByParentIdOrderByIdDesc(Long parentId);
    long countByParentIdAndReadFlagFalse(Long parentId);
}
