package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.ParentEmergencyAlert;

@Repository
public interface ParentEmergencyAlertRepository extends JpaRepository<ParentEmergencyAlert, Long> {
}
