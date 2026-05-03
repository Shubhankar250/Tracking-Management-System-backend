package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertDeviceCommandMapping;

public interface AlertDeviceCommandMappingRepository extends JpaRepository<AlertDeviceCommandMapping, Long> {

    List<AlertDeviceCommandMapping> findByAlert(Alert alert);
    void deleteByAlertId(Long alertId);
}

