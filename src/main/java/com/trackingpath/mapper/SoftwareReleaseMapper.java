package com.trackingpath.mapper;

import com.trackingpath.dtos.SoftwareReleaseDTO;
import com.trackingpath.entities.SoftwareReleaseEntity;

public class SoftwareReleaseMapper {

    public static SoftwareReleaseDTO toDTO(SoftwareReleaseEntity entity) {

        if (entity == null)
            return null;

        SoftwareReleaseDTO dto = new SoftwareReleaseDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setText(entity.getText());
        dto.setUserId(entity.getUserId());

        return dto;
    }

    public static SoftwareReleaseEntity toEntity(SoftwareReleaseDTO dto) {

        if (dto == null)
            return null;

        SoftwareReleaseEntity entity = new SoftwareReleaseEntity();

        entity.setDate(dto.getDate());
        entity.setText(dto.getText());
        entity.setUserId(dto.getUserId());

        return entity;
    }
}