package com.trackingpath.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.DeviceGroupDataProjection;
import com.trackingpath.dtos.DeviceModalDTO;
import com.trackingpath.dtos.DeviceModalSearchDTO;
import com.trackingpath.entities.DeviceModalEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceModalRepository;

@Service
public class DeviceModalService {

	@Autowired
	private DeviceModalRepository deviceModalRepository;

	public Map<String, Object> getAllDeviceModals(Users user, int page, int size, String search) {

		Pageable pageable = PageRequest.of(page, size);

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";

		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

		Page<DeviceModalDTO> modalPage = deviceModalRepository.findDeviceModalsWithSearch(user.getAdminId(),
				user, isAdmin, searchParam, pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("data", modalPage.getContent());
		response.put("currentPage", modalPage.getNumber());
		response.put("totalItems", modalPage.getTotalElements());
		response.put("totalPages", modalPage.getTotalPages());

		return response;
	}

	public DeviceModalDTO getById(Long id) {

		return deviceModalRepository.findById(id).map(this::convertToDTO).orElse(null);
	}

	public boolean insertDeviceModal(DeviceModalDTO dto, Users user) {

		DeviceModalEntity entity = convertToEntity(dto);
		entity.setUserId(user);
        entity.setAdminId(user.getAdminId());
		deviceModalRepository.save(entity);

		return true;
	}

	public boolean updateDeviceModal(Long id, DeviceModalDTO dto, Users user) {

		Optional<DeviceModalEntity> optional = deviceModalRepository.findById(id);

		if (!optional.isPresent()) {
			return false;
		}

		DeviceModalEntity existing = optional.get();

		if (dto.getCompanyName() != null)
			existing.setCompanyName(dto.getCompanyName()); 
		
		if (dto.getModalName() != null)
			existing.setModalName(dto.getModalName());

		if (dto.getModalType() != null)
			existing.setModalType(dto.getModalType());

		if (dto.getNoOfChannel() != null)
			existing.setNoOfChannel(dto.getNoOfChannel());

		if (dto.getUserManual() != null)
			existing.setUserManual(dto.getUserManual());

		if (dto.getProtocolManual() != null)
			existing.setProtocolManual(dto.getProtocolManual());

		if (dto.getCommands() != null)
			existing.setCommands(dto.getCommands());

		if (dto.getConnectedIP() != null)
			existing.setConnectedIP(dto.getConnectedIP());

		if (dto.getConnectedPort() != null)
			existing.setConnectedPort(dto.getConnectedPort());

		// Image handling
		if (dto.getImage() != null) {
			existing.setImage(dto.getImage());
		}
		if (dto.getNoOfDIN() != null)
            existing.setNoOfDIN(dto.getNoOfDIN());
        
        if (dto.getNoOfAIN() != null)
            existing.setNoOfAIN(dto.getNoOfAIN());
        
        if (dto.getNoOfDOUT() != null)
            existing.setNoOfDOUT(dto.getNoOfDOUT());
        
        if (dto.getProtocolName() != null)
            existing.setProtocolName(dto.getProtocolName());
        
        
        if (dto.getAdasAlertType() != null)
            existing.setAdasAlertType(dto.getAdasAlertType()); 
                
                if (dto.getDmsAlertType() != null)
            existing.setDmsAlertType(dto.getDmsAlertType());
        
        
        existing.setActive(dto.getActive());
		deviceModalRepository.save(existing);

		return true;
	}

	public boolean deleteDeviceModal(Long id, Users user) {

		if (!deviceModalRepository.existsById(id)) {
			return false;
		}

		deviceModalRepository.deleteById(id);
		return true;
	}

	private DeviceModalDTO convertToDTO(DeviceModalEntity entity) {

		DeviceModalDTO dto = new DeviceModalDTO();

		dto.setId(entity.getId());
		dto.setCompanyName(entity.getCompanyName());
		dto.setModalName(entity.getModalName());
		dto.setModalType(entity.getModalType());
		dto.setNoOfChannel(entity.getNoOfChannel());
		dto.setImage(entity.getImage());
		dto.setUserManual(entity.getUserManual());
		dto.setProtocolManual(entity.getProtocolManual());
		dto.setCommands(entity.getCommands());
		dto.setConnectedIP(entity.getConnectedIP());
		dto.setConnectedPort(entity.getConnectedPort());
		dto.setNoOfDIN(entity.getNoOfDIN());
		dto.setNoOfAIN(entity.getNoOfAIN());
		dto.setNoOfDOUT(entity.getNoOfDOUT());
		dto.setProtocolName(entity.getProtocolName());
        dto.setActive(entity.getActive());
		return dto;
	}

	private DeviceModalEntity convertToEntity(DeviceModalDTO dto) {

		DeviceModalEntity entity = new DeviceModalEntity();
        entity.setCompanyName(dto.getCompanyName());
		entity.setModalName(dto.getModalName());
		entity.setModalType(dto.getModalType());
		entity.setNoOfChannel(dto.getNoOfChannel());
		entity.setImage(dto.getImage());
		entity.setUserManual(dto.getUserManual());
		entity.setProtocolManual(dto.getProtocolManual());
		entity.setCommands(dto.getCommands());
		entity.setConnectedIP(dto.getConnectedIP());
		entity.setConnectedPort(dto.getConnectedPort());
		entity.setNoOfDIN(dto.getNoOfDIN());
		entity.setNoOfAIN(dto.getNoOfAIN());
		entity.setNoOfDOUT(dto.getNoOfDOUT());
		entity.setProtocolName(dto.getProtocolName());
		entity.setAdasAlertType(dto.getAdasAlertType());
        entity.setDmsAlertType(dto.getDmsAlertType());
        entity.setActive(dto.getActive());

		return entity;
	}

    public List<String> getAllModalNames() {
        return deviceModalRepository.findAllModalNames();
}

    public List<DeviceModalSearchDTO> searchDeviceModal(String keyword) {

        return deviceModalRepository.searchDeviceModal(keyword);

    }

    public List<String> getAllCompanyNames() {
        return deviceModalRepository.findAllCompanyNames();
    }

    public List<DeviceModalDTO> getDevicesByCompanyName(String companyName) {
      
           return deviceModalRepository.findAllByCompanyName(companyName);
    }

    public List<DeviceGroupDataProjection> getGroupedDevices(List<Long> deviceIds) {
        return deviceModalRepository.getGroupedDevices(deviceIds);
}
}
