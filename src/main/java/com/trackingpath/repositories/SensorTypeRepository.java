package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.SensorTypeDTO;
import com.trackingpath.entities.SensorType;

@Repository
public interface SensorTypeRepository extends JpaRepository<SensorType, Long> {

	List<SensorTypeDTO> findAllProjectedBy();
}
