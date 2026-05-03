package com.trackingpath.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.DevicesUserMapping;

public interface DevicesUserMappingRepository extends JpaRepository<DevicesUserMapping, Long>{

	  Optional<DevicesUserMapping> findByDeviceId(Long deviceId);
}
