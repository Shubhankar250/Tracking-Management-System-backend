package com.trackingpath.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.ParentNotificationPreference;

@Repository
public interface ParentNotificationPreferenceRepository extends JpaRepository<ParentNotificationPreference, Long> {
    Optional<ParentNotificationPreference> findByParentId(Long parentId);
}
