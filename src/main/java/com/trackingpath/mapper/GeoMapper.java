package com.trackingpath.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.GeofenceDTO;

import com.trackingpath.entities.Geofence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class GeoMapper {

	@PersistenceContext
	private EntityManager entityManager;

	
	  
	  public GeofenceDTO toDto(Geofence entity) {
	  
	  GeofenceDTO dto = new GeofenceDTO();
	  
	  dto.setId(entity.getId()); 
	  dto.setGeo_group(entity.getGeo_group());
	  
	  dto.setPcts_type(entity.getPcts_type());
	  dto.setColor(entity.getColor());
	  dto.setPcts_name(entity.getName());
	  dto.setSpeed_limit(entity.getSpeed_limit());
	
	  return dto; }
	 
	

	

	  public Geofence toEntity(GeofenceDTO dto) {
			Geofence entity = new Geofence();
			
			entity.setName(dto.getPcts_name());
			entity.setGeo_group(dto.getGeo_group());
			entity.setPcts_type(dto.getPcts_type());
			entity.setColor(dto.getColor());
		
			entity.setSpeed_limit(dto.getSpeed_limit());
			entity.setRadius(dto.getRadius());

			return entity;
		}

		public void updateEntityFromDto(GeofenceDTO dto, Geofence e) {

			e.setName(dto.getPcts_name());
			e.setPcts_type(dto.getPcts_type());
			e.setColor(dto.getColor());
			e.setGeo_group(dto.getGeo_group());
			e.setSpeed_limit(dto.getSpeed_limit());
			e.setRadius(dto.getRadius());
		}
		
		

		public GeofenceDTO toDto(Object[] row) {

			GeofenceDTO dto = new GeofenceDTO();
		    ObjectMapper mapper = new ObjectMapper();

		
		    dto.setId((Long) row[0]); 
			dto.setPcts_name((String) row[01]);
			dto.setColor((String) row[2]);
			 try {
			        //  Convert GeoJSON string to JSON object
			        dto.setGeom(mapper.readValue((String) row[3], Object.class));
			    } catch (Exception e) {
			        dto.setGeom(null);
			    }
			dto.setPcts_type((String) row[4]);
			dto.setGeo_group((String) row[5]); // STRING
			dto.setSpeed_limit((String) row[6]); // STRING
		    if (row[6] != null) {
	            dto.setRadius(((Number) row[7]).doubleValue());
	        }

			return dto;
		}

	}
