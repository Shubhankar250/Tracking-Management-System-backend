package com.trackingpath.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.ReportMaster;

@Repository
public interface ReportMasterRepository extends JpaRepository<ReportMaster, Integer> {

    Optional<ReportMaster> findByReportType(String reportType);
}
