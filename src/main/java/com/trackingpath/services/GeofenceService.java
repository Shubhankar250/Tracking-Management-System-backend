package com.trackingpath.services;


import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.GeoGroupDTO;
import com.trackingpath.dtos.GeofenceDTO;

import com.trackingpath.entities.GeoGroups;
import com.trackingpath.entities.Geofence;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.GeoGroupMapper;
import com.trackingpath.mapper.GeoMapper;
import com.trackingpath.repositories.GeofenceRepository;
import com.trackingpath.repositories.GeoGroupRepository;

import jakarta.transaction.Transactional;

@Service
public class GeofenceService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	GeofenceRepository geoRepo;
	@Autowired
	GeoMapper geoMapper;
  @Autowired
  GeoGroupRepository groupRepo;
  
  @Autowired
  GeoGroupMapper geoGroupMapper;
  
  public Map<String, Object> getGeofence(Users user, int page, int size, String search) {

	    String searchParam = "%" + (search != null ? search.trim() : "") + "%";

	    Pageable pageable = PageRequest.of(page, size);

	    boolean isAdmin = user.getRoles().stream()
	            .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

	    Page<Object[]> geoPage;

	    if (isAdmin) {
	        geoPage = geoRepo.findGeofenceByAdmin(
	                user.getAdminId(),
	                searchParam,
	                pageable
	        );
	    } else {
	        geoPage = geoRepo.findGeofenceByUser(
	                user.getId(),
	                searchParam,
	                pageable
	        );
	    }

	    List<GeofenceDTO> dtoList = geoPage.getContent()
	            .stream()
	            .map(geoMapper::toDto)
	            .toList();

	    return Map.of(
	            "data", dtoList,
	            "currentPage", geoPage.getNumber(),
	            "totalItems", geoPage.getTotalElements(),
	            "totalPages", geoPage.getTotalPages()
	    );
	}
	
  @Transactional
  public boolean createGeofence(GeofenceDTO geo, Users user) {
	 // Geofence entity = geoMapper.toEntity(geo);

	 // entity.setAdmin_id(user.getAdminId());
	 // entity.setUser_id(user.getId());
	  
	  ObjectMapper mapper = new ObjectMapper();
	    String geoJson = null;
		try {
			geoJson = mapper.writeValueAsString(geo.getGeom());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    geoRepo.insertGeofence(
	        geo.getPcts_name(),
	        geoJson,
	        geo.getColor(),
	        geo.getPcts_type(),
	        geo.getGeo_group(),
	        geo.getSpeed_limit(),
	        geo.getRadius(),
	        user.getAdminId(),
	        user.getId()
	    );
	    return true;

       
}

	public boolean updateGeofencing(Long id, GeofenceDTO newGeofence) {

	    return geoRepo.findById(id).map(oldGeofence -> {

	        geoMapper.updateEntityFromDto(newGeofence, oldGeofence);
	        geoRepo.save(oldGeofence);
	        
	        if (newGeofence.getGeom() != null) {
	            String geoJson = null;
				try {
					geoJson = new ObjectMapper()
					        .writeValueAsString(newGeofence.getGeom());
				} catch (JsonProcessingException e) {
					
					e.printStackTrace();
				}
	            geoRepo.updateGeom(id, geoJson);
	        }
	        return true;

	    }).orElse(false);
	}

	public boolean deleteGeofence(Long id) {
		 if(geoRepo.existsById(id)) {
			 geoRepo.deleteById(id);
			 return true;
		 }else {
			 return false;
		 }
		 
	}

	public Map<Long, String> getAllGeoGroupName(Users user) {
		return groupRepo.findGeoGroups(user.getAdminId())
	            .stream()
	            .collect(Collectors.toMap(
	                GeoGroupDTO::getGroup_id,
	                GeoGroupDTO::getGroup_name
	            ));
	  
	}

	public Map<Long, String> getAllGeofence() {
		
		
		
		    return geoRepo.findAllGeofence()
		            .stream()
		            .filter(g -> g.getId() != null && g.getName() != null)
		            .collect(Collectors.toMap(
		                    Geofence::getId,
		                    Geofence::getName
		            ));
		

		}


	  public boolean addDatageoGroup(String groupName, Users user) {
	        try {
	            GeoGroups entity = GeoGroups.builder()
	                    .name(groupName)
	                    .user_id(user.getId())
	                    .admin_id(user.getAdminId())
	                    .creation_time(LocalDateTime.now())
	                    .build();

	            groupRepo.save(entity);
	            return true;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	 @Transactional
	  public boolean deleteGeoGroup(Long id, Users user) {
	      int rows = groupRepo.deleteByIdAndUserId(id, user.getId());
	      return rows > 0;
	  }



	
	

}


