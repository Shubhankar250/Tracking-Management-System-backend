package com.trackingpath.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.RoutesDTO;
import com.trackingpath.entities.Poi;
import com.trackingpath.entities.PoiGroup;
import com.trackingpath.entities.RouteGroup;
import com.trackingpath.entities.Routes;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.RouteMapper;
import com.trackingpath.repositories.RouteGroupRepository;
import com.trackingpath.repositories.RouteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Service
@RequiredArgsConstructor
public class RouteService {
	@Autowired
    private RouteGroupRepository routeGroupRepository;

    private final RouteRepository routeRepository;

    /* ---------------- CREATE ---------------- */
 

    public boolean createRoutes(RoutesDTO bean, Long userId, Long adminId) {
        try {
            routeRepository.insertRoute(
                bean.getName(),
                bean.getDescription(),
                bean.getGroup(),
                bean.getBuffer(),
                bean.getGeom(),   // GeoJSON string
                userId,
                adminId
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /* ---------------- READ ALL ---------------- */
    public Map<String, Object> getRoutes(int page, int size, String search) {

        String searchParam = "%" + (search != null ? search.trim() : "") + "%";

        Pageable pageable = PageRequest.of(page, size);

        Page<Map<String, Object>> routePage =
                routeRepository.findAllRoutesAsGeoJson(searchParam, pageable);

        return Map.of(
                "data", routePage.getContent(),
                "currentPage", routePage.getNumber(),
                "totalItems", routePage.getTotalElements(),
                "totalPages", routePage.getTotalPages()
        );
    }



    /* ---------------- READ BY ID ---------------- */
    public Map<String, Object> getRouteById(long id) {
        return routeRepository.findRouteByIdAsGeoJson(id);
    }


    /* ---------------- DELETE ---------------- */
    public boolean deleteRoutes(long id) {
        if (!routeRepository.existsById(id)) return false;

        routeRepository.deleteById(id);
        return true;
    }

    /* ---------------- UPDATE ---------------- */
    @Transactional
    public boolean updateRoute(RoutesDTO dto) {

        int rows;

        if (dto.getGeom() == null || "null".equalsIgnoreCase(dto.getGeom())) {
            // ❌ geom NOT updated
            rows = routeRepository.updateRouteWithoutGeom(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getGroup(),
                dto.getBuffer()
            );
        } else {
            // ✅ geom updated
            rows = routeRepository.updateRouteWithGeom(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getGroup(),
                dto.getBuffer(),
                dto.getGeom()
            );
        }

        return rows > 0;
    }

    
    public boolean addDataRouteGroup(String groupName, Users user) {
        try {
            RouteGroup group = new RouteGroup();
            group.setName(groupName);
            group.setUserId(user.getId());
            group.setAdminId(user.getAdminId());
            group.setCreationTime(LocalDateTime.now());
            routeGroupRepository.save(group);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRouteGroup(Integer id, Users user) {
        try {
            int rows = routeGroupRepository.deleteByIdAndUserId(id, user.getId());
            return rows > 0; // ✅ real check
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	public Map<Integer, String> getAllRouteGroupName(Users user) {
		  List<RouteGroup> groups = routeGroupRepository.findByAdminId(user.getAdminId());
	        Map<Integer, String> map = new LinkedHashMap<>();
	        for (RouteGroup g : groups) {
	            map.put(g.getId(), g.getName());
	        }
	        return map;
	}

	public Map<Long, String> getAllRoute() {
		
		
		
	    return routeRepository.findAllRoute()
	            .stream()
	            .filter(r -> r.getId() != null && r.getName() != null)
	            .collect(Collectors.toMap(
	            		Routes::getId,
	            		Routes::getName
	            ));
	

	}
    
}
