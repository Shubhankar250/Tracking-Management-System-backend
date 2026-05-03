package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertDeviceMapping;

public interface AlertDeviceMappingRepository extends JpaRepository<AlertDeviceMapping, Long> {

    List<AlertDeviceMapping> findByAlert(Alert alert);

    void deleteByAlertId(Long alertId);
    
}


