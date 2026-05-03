package com.trackingpath.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.SoftwareReleaseDTO;
import com.trackingpath.entities.SoftwareReleaseEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.SoftwareReleaseMapper;
import com.trackingpath.repositories.SoftwareReleaseRepository;

@Service
public class SoftwareReleaseService {
	@Autowired
	private SoftwareReleaseRepository repository;

	public boolean addSoftwareRelease(SoftwareReleaseDTO dto, Users user) {

	    SoftwareReleaseEntity entity = SoftwareReleaseMapper.toEntity(dto);

	    entity.setUserId(user.getId());
	    repository.save(entity);
		return true;
	}
	
	
	
	
    public Map<String, Object> getSoftwareReleaseList(Users user, int page, int pageSize, String search) {

        List<SoftwareReleaseEntity> list = repository.findAll();

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("total", list.size());

        return result;
    }
    public SoftwareReleaseDTO getSoftwareReleaseById(long id) {

        SoftwareReleaseEntity entity = repository.findById(id).orElse(null);

        return SoftwareReleaseMapper.toDTO(entity);
    }




    public boolean updateSoftwareRelease(SoftwareReleaseEntity release, Users user) {

        Optional<SoftwareReleaseEntity> optional = repository.findById(release.getId());

        if (optional.isPresent()) {

            SoftwareReleaseEntity entity = optional.get();

            entity.setDate(release.getDate());
            entity.setText(release.getText());

            repository.save(entity);

            return true;
        }

        return false;
    }



    public boolean deleteSoftwareRelease(long id) {

        if (repository.existsById(id)) {

            repository.deleteById(id);
            return true;
        }

        return false;
    }
	
	
}
