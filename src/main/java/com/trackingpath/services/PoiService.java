package com.trackingpath.services;

import com.trackingpath.dtos.PoiDto;
import com.trackingpath.entities.Geofence;
import com.trackingpath.entities.Poi;
import com.trackingpath.entities.PoiGroup;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.PoiMapper;
import com.trackingpath.repositories.PoiGroupRepository;
import com.trackingpath.repositories.PoiRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoiService {

    @Autowired
    private PoiRepository poiRepository;

    @Autowired
    private PoiGroupRepository poiGroupRepository;

    public PoiDto createPoi(PoiDto dto, Users user) {
        Poi p = PoiMapper.toEntity(dto);
        p.setUser(user);
        p.setAdminId(user.getAdminId());
        p.setCreatedAt(LocalDateTime.now());
        if (dto.getPoiGroupId() != null) {
            poiGroupRepository.findById(dto.getPoiGroupId()).ifPresent(p::setPoiGroup);
        }
        Poi saved = poiRepository.save(p);
        return PoiMapper.toDto(saved);
    }

    public Map<String, Object> getAllPois(Users user, int page, int size, String search) {

        String searchParam = "%" + (search != null ? search.trim() : "") + "%";

        Pageable pageable = PageRequest.of(page, size);

        Page<Poi> poiPage = poiRepository.findPoiByAdminId(
                user.getAdminId(),
                searchParam,
                pageable
        );

        List<PoiDto> dtoList = poiPage.getContent()
                .stream()
                .map(PoiMapper::toDto)
                .toList();

        return Map.of(
                "data", dtoList,
                "currentPage", poiPage.getNumber(),
                "totalItems", poiPage.getTotalElements(),
                "totalPages", poiPage.getTotalPages()
        );
    }

    public Optional<PoiDto> getPoiById(Long id, Users user) {
        return poiRepository.findById(id)
                .filter(p -> Objects.equals(p.getAdminId(), user.getAdminId()))
                .map(PoiMapper::toDto);
    }

    @Transactional
    public boolean updatePoi(PoiDto dto, Users user) {
        Optional<Poi> existingOpt = poiRepository.findById(dto.getId());
        if (existingOpt.isEmpty()) return false;
        Poi p = existingOpt.get();
        if (!Objects.equals(p.getAdminId(), user.getAdminId())) return false;

        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getMarkerIcon() != null) p.setMarkerIcon(dto.getMarkerIcon());
        if (dto.getRadius() != null) p.setRadius(dto.getRadius());
        if (dto.getLatitude() != null) p.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) p.setLongitude(dto.getLongitude());
        if (dto.getPoiGroupId() != null) {
            poiGroupRepository.findById(dto.getPoiGroupId()).ifPresent(p::setPoiGroup);
        }
        p.setUpdatedAt(LocalDateTime.now());
        poiRepository.save(p);
        return true;
    }

    public boolean deletePoi(Long id, Users user) {
        Optional<Poi> existing = poiRepository.findById(id);
        if (existing.isEmpty()) return false;
        Poi p = existing.get();
        if (!Objects.equals(p.getAdminId(), user.getAdminId())) return false;
        poiRepository.delete(p);
        return true;
    }

    public Map<Integer, String> getAllPoiGroupName(Users user) {
        List<PoiGroup> groups = poiGroupRepository.findByAdminId(user.getAdminId());
        Map<Integer, String> map = new LinkedHashMap<>();
        for (PoiGroup g : groups) {
            map.put(g.getId(), g.getName());
        }
        return map;
    }

    public boolean addPoiGroup(String groupName, Users user) {
        PoiGroup g = PoiGroup.builder()
                .name(groupName)
                .user(user)
                .adminId(user.getAdminId())
                .creationTime(LocalDateTime.now())
                .build();
        poiGroupRepository.save(g);
        return true;
    }

    public boolean deletePoiGroup(Integer id, Users user) {
        Optional<PoiGroup> g = poiGroupRepository.findById(id);
        if (g.isEmpty()) return false;
        if (!Objects.equals(g.get().getAdminId(), user.getAdminId())) return false;
        poiGroupRepository.deleteByIdAndAdminId(id,user.getAdminId());
        return true;
    }

	
	public Map<Long, String> getAllPoi() {
		
		
		
	    return poiRepository.findAllPoi()
	            .stream()
	            .filter(p -> p.getId() != null && p.getName() != null)
	            .collect(Collectors.toMap(
	            		Poi::getId,
	            		Poi::getName
	            ));
	

	}

}
