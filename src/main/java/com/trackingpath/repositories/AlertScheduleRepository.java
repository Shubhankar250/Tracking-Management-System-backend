package com.trackingpath.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.Alert;
import com.trackingpath.entities.AlertSchedule;

public interface AlertScheduleRepository extends JpaRepository<AlertSchedule, Long> {

    List<AlertSchedule> findByAlert(Alert alert);

    //void deleteByAlert(Alert alert);
    void deleteByAlertId(Long alertId);

}

