package com.trackingpath.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceGroupDataDto {

    private Long device_id;
    private String device_name;
    private Long group_id;
    private String group_name;

    // 🔴 EXPLICIT constructor for JPQL
    public DeviceGroupDataDto(
            Long device_id,
            String device_name,
            Long group_id,
            String group_name
    ) {
        this.device_id = device_id;
        this.device_name = device_name;
        this.group_id = group_id;
        this.group_name = group_name;
    }
}
