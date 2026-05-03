package com.trackingpath.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.trackingpath.dtos.DeviceDto;
import com.trackingpath.dtos.DeviceSettingDto;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Users;


@Component
public class DeviceMapper {


    public DeviceEntity toEntity(DeviceSettingDto dto, Users currentUser) {
        if (dto == null) return null;

        return DeviceEntity.builder()
                .name(dto.getName())
                .uniqueid(dto.getUniqueid())
                .simCardNumber(dto.getSimCardNumber())
                .simActivationDate(dto.getSimActivationDate())
                .simExpirationDate(dto.getSimExpirationDate())
                .vin(dto.getVin())
                .deviceModel(dto.getDeviceModel())
                .installationDate(dto.getInstallationDate())
                .plateNumber(dto.getPlateNumber())
                .registrationNumber(dto.getRegistrationNumber())
                .owner(dto.getOwner())
                .fuelMeasureName(dto.getFuelMeasureName())
                .fuelMeasurement(dto.getFuelMeasurement())
                .fuelCost(dto.getFuelCost())
                .iconType(dto.getIconType())
                .movingIconColor(dto.getMovingIconColor())
                .stoppedIconColor(dto.getStoppedIconColor())
                .offlineIconColor(dto.getOfflineIconColor())
                .engineIdleColor(dto.getEngineIdleColor())
                .status(dto.getStatus())
                .sensors(dto.getSensors())
                .tailColor(dto.getTailColor())
                .tailLength(dto.getTailLength())
                .imgIconName(dto.getImgIconName())
                .imgIconType(dto.getImgIconType())
                .odometer(dto.getOdometer())
                .vehicleStatus(dto.getVehicleStatus())
                .maxSpeed(dto.getMaxSpeed())
                .minMovingSpeed(dto.getMinMovingSpeed())
                .minFuelFillings(dto.getMinFuelFillings())
                .minFuelTheft(dto.getMinFuelTheft())
                .fuelChangeAfterStop(dto.getFuelChangeAfterStop())
                .objectIcon(dto.getObjectIcon())
                .devicetimezone(dto.getDeviceTimezone())
                .userId(currentUser.getId())
                .build();
    }
}
