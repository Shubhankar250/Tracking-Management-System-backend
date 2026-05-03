package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertPoiMapping;

public interface AlertPoiMappingRepository extends JpaRepository<AlertPoiMapping, Long> {

    List<AlertPoiMapping> findByAlert(Alert alert);
    void deleteByAlertId(Long alertId);

}

