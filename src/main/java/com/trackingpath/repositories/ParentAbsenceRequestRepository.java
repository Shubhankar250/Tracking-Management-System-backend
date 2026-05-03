package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.ParentAbsenceRequest;

@Repository
public interface ParentAbsenceRequestRepository extends JpaRepository<ParentAbsenceRequest, Long> {
}
