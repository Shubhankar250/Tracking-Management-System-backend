package com.trackingpath.repositories;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertGeofenceMapping;

public interface AlertGeofenceMappingRepository extends JpaRepository<AlertGeofenceMapping, Long> {

    List<AlertGeofenceMapping> findByAlert(Alert alert);

    //void deleteByAlert(Alert alert);
    void deleteByAlertId(Long alertId);
}

