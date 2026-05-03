package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertRouteMapping;

public interface AlertRouteMappingRepository extends JpaRepository<AlertRouteMapping, Long> {

    List<AlertRouteMapping> findByAlert(Alert alert);

    //void deleteByAlert(Alert alert);
    
    void deleteByAlertId(Long alertId);
}

