package com.trackingpath.repositories;

import com.trackingpath.dtos.SharePositionBean;
import com.trackingpath.entities.SharePositionScheduleEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharePositionScheduleRepository
        extends JpaRepository<SharePositionScheduleEntity, Long> {

    Optional<SharePositionScheduleEntity> findByShareId(Long shareId);

    void deleteByShareId(Long shareId);

   
}
