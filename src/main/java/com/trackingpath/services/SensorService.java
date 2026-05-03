package com.trackingpath.services;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DeviceSensorMappingDTO;
import com.trackingpath.dtos.SensorDTO;
import com.trackingpath.dtos.SensorListDTO;
import com.trackingpath.dtos.SensorTypeDTO;
import com.trackingpath.entities.DeviceSensorMapping;
import com.trackingpath.entities.LiveData;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.SensorMapper;
import com.trackingpath.repositories.LiveDataRepository;
import com.trackingpath.repositories.SensorRepository;
import com.trackingpath.repositories.SensorTypeRepository;


@Service
public class SensorService {

    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private  SensorTypeRepository sensorTypeRepository;
    @Autowired
    private LiveDataRepository liveDataRepository;

	@Autowired
    private ObjectMapper objectMapper;

	
	public List<SensorTypeDTO> getSensorType(Users user) {
		// TODO Auto-generated method stub
			return sensorTypeRepository.findAllProjectedBy();
	}
	
    public Map<String, Object> getAttributeById(long deviceId, Users user) {

    	LiveData data = liveDataRepository.findByDeviceId(deviceId).orElse(null);

        if (data == null || data.getAttributes() == null)
            return new HashMap<>();

        try {
            return objectMapper.readValue(data.getAttributes(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
	
    // ADD
    public String addSensorData(SensorDTO bean, Users user) {

        DeviceSensorMapping entity = SensorMapper.toEntity(bean, user);
        sensorRepository.save(entity);
        return "add successfully !";
    }


    // GET BY ID
    public DeviceSensorMappingDTO getSensorById(Long id, Users user) {

        DeviceSensorMapping entity =
                sensorRepository.findById(id)
                .filter(e -> e.getAdminId().equals(user.getAdminId()))
                .orElse(null);

        if (entity == null) return null;

        return SensorMapper.toDto(entity);
    }


    // LIST ALL
    public Page<DeviceSensorMappingDTO> listSensors(Long deviceId, Users user, Pageable pageable) {

        Page<DeviceSensorMapping> pageResult =
                sensorRepository.findByAdminIdAndDeviceId(user.getAdminId(), deviceId, pageable);

        return pageResult.map(SensorMapper::toDto);
    }


    // UPDATE
    public String updateSensor(SensorDTO bean, Users user) {

        DeviceSensorMappingDTO d = bean.getDeviceSensorMappingBean();

        DeviceSensorMapping entity =
                sensorRepository.findById(d.getId())
                .filter(e -> e.getAdminId().equals(user.getAdminId()))
                .orElse(null);

        SensorMapper.updateEntity(entity, bean);
        sensorRepository.save(entity);

        return "Updated successfully!";
    }



    // DELETE
    public String deleteSensor(Long id, Users user) {

        DeviceSensorMapping entity =
                sensorRepository.findById(id)
                .filter(e -> e.getAdminId().equals(user.getAdminId()))
                .orElse(null);

        if (entity == null) return "Sensor Not Found!";

        sensorRepository.delete(entity);

        return "Deleted successfully!";
    }

    public List<SensorListDTO> getSensorData(Users user, long deviceId) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));
        Long adminId = isAdmin ? user.getAdminId() : null;
        Long userId = isAdmin ? null : user.getId();

        return sensorRepository.findSensorData(deviceId, isAdmin, adminId, userId);
    }

}
