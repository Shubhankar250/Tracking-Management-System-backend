package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertDetails;

public interface AlertDetailsRepository extends JpaRepository<AlertDetails, Long> {

    List<AlertDetails> findByAlert(Alert alert);

    void deleteByAlertId(Long alertId);
}

