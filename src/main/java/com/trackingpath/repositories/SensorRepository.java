package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trackingpath.dtos.SensorListDTO;
import com.trackingpath.entities.DeviceSensorMapping;

@Repository
public interface SensorRepository extends JpaRepository<DeviceSensorMapping, Long> {

	List<DeviceSensorMapping> findByDeviceId(Long deviceId);

	Page<DeviceSensorMapping> findByAdminIdAndDeviceId(Long adminId, Long deviceId, Pageable pageable);

	List<DeviceSensorMapping> findByUserId(Long userId);

	@Query("SELECT new com.trackingpath.dtos.SensorListDTO(d.id, d.name, s.sensorTypeName, d.parameter) "
			+ "FROM DeviceSensorMapping d " + "JOIN SensorType s ON d.sensorTypeId = s.id "
			+ "WHERE d.deviceId = :deviceId " + "AND ((:isAdmin = true AND d.adminId = :adminId) "
			+ "OR (:isAdmin = false AND d.userId = :userId))")
	List<SensorListDTO> findSensorData(@Param("deviceId") Long deviceId, @Param("isAdmin") boolean isAdmin,
			@Param("adminId") Long adminId, @Param("userId") Long userId);
}
