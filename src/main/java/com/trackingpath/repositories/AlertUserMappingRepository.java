package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertUserMapping;

public interface AlertUserMappingRepository extends JpaRepository<AlertUserMapping, Long> {

    List<AlertUserMapping> findByAlert(Alert alert);

    //void deleteByAlert(Alert alert);
    void deleteByAlertId(Long alertId);
}

