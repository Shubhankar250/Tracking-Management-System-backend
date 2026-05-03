package com.trackingpath.mapper;

import org.springframework.stereotype.Component;
import com.trackingpath.dtos.ExternalAccessTokenDTO;
import com.trackingpath.entities.ExternalAccessToken;

@Component
public class ExternalAccessTokenMapper {

    public ExternalAccessToken toEntity(ExternalAccessTokenDTO dto) {
        if (dto == null) return null;

        ExternalAccessToken entity = new ExternalAccessToken();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setProjectName(dto.getProjectName()); 
        entity.setExternalAccessToken(dto.getExternalAccessToken());
        entity.setUrl(dto.getUrl());

        return entity;
    }

    public ExternalAccessTokenDTO toDTO(ExternalAccessToken entity) {
        if (entity == null) return null;

        ExternalAccessTokenDTO dto = new ExternalAccessTokenDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setProjectName(entity.getProjectName()); 
        dto.setExternalAccessToken(entity.getExternalAccessToken());
        dto.setUrl(entity.getUrl());

        return dto;
    }
}
