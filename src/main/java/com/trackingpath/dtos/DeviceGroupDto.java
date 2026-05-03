package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceGroupDto {

    private Long deviceId;
    private String name;
    private String uniqueid;
    private String plateNumber;
    private String vehicleStatus;
    private String deviceModel;

    private Long groupId;
    private String groupName;


    public DeviceGroupDto(
            Long deviceId,
            String name,
            String uniqueid,
            String plateNumber,
            String vehicleStatus,
            String deviceModel,
            Long groupId,
            String groupName
    ) {
        this.deviceId = deviceId;
        this.name = name;
        this.uniqueid = uniqueid;
        this.plateNumber = plateNumber;
        this.vehicleStatus = vehicleStatus;
        this.deviceModel = deviceModel;
        this.groupId = groupId;
        this.groupName = groupName;
    }

}
