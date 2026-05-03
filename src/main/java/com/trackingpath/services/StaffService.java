package com.trackingpath.services;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.StaffDetails;
import com.trackingpath.repositories.StaffDetailsRepository;
import com.trackingpath.dtos.StaffDto;


@Service
public class StaffService {

	@Autowired 
	StaffDetailsRepository repo;

	public StaffDto save(StaffDto dto) {
		System.out.println("FULL DTO = " + dto);
		System.out.println("NAME = " + dto.getName());
		System.out.println("EMAIL = " + dto.getEmail());
	    StaffDetails entity;

	    // ✅ FIX
	    if (dto.getId() != null && dto.getId() > 0) {
	        entity = repo.findById(dto.getId())
	                .orElseThrow(() -> new RuntimeException("Staff not found"));
	    } else {
	        entity = new StaffDetails();
	    }

	    entity.setName(dto.getName());
	    entity.setDesignation(dto.getDesignation());
	    entity.setEmail(dto.getEmail());
	    entity.setEmployeeCode(dto.getEmployeeCode());
	    entity.setMobileNumber(dto.getMobileNumber());

	    StaffDetails saved = repo.save(entity);

	    return mapToDto(saved);
	}

    public Map<String, Object> getStaff(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        String searchParam = "%" + (search != null ? search.trim() : "") + "%";

        Page<StaffDetails> staffPage =
                repo.findBySearch(searchParam, pageable);

        List<StaffDto> list = staffPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("currentPage", staffPage.getNumber());
        response.put("totalItems", staffPage.getTotalElements());
        response.put("totalPages", staffPage.getTotalPages());

        return response;
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private StaffDto mapToDto(StaffDetails e) {
        return StaffDto.builder()
                .id(e.getId())
                .name(e.getName())
                .designation(e.getDesignation())
                .email(e.getEmail())
                .employeeCode(e.getEmployeeCode())
                .mobileNumber(e.getMobileNumber())
                .build();
    }
    public StaffDto getById(Long id) {
        StaffDetails entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        return mapToDto(entity);
    }
}