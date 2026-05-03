package com.trackingpath.mapper;

import com.trackingpath.dtos.PoiDto;
import com.trackingpath.entities.Poi;
import com.trackingpath.entities.PoiGroup;

public class PoiMapper {

    public static PoiDto toDto(Poi p) {
        if (p == null) return null;
        PoiDto dto = new PoiDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        if (p.getPoiGroup() != null) {
            dto.setPoiGroupId(p.getPoiGroup().getId());
            dto.setPoiGroupName(p.getPoiGroup().getName());
        }
        dto.setMarkerIcon(p.getMarkerIcon());
        dto.setRadius(p.getRadius());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        return dto;
    }

    public static Poi toEntity(PoiDto dto) {
        if (dto == null) return null;
        Poi p = new Poi();
        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setMarkerIcon(dto.getMarkerIcon());
        p.setRadius(dto.getRadius());
        p.setLatitude(dto.getLatitude());
        p.setLongitude(dto.getLongitude());
        if (dto.getPoiGroupId() != null) {
            PoiGroup g = new PoiGroup();
            g.setId(dto.getPoiGroupId());
            p.setPoiGroup(g);
        }
        return p;
    }
}
