package com.trackingpath.dtos;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevicesDetailsDto {

	private DevicesUpdateDto deviceData;
    private LiveDataBean liveData;

    private Map<Long, String> groupNames;
    private Map<Long, String> userMap;

    private List<String> deviceModels;
    private List<SensorListDTO> sensorData;

    private List<MaintenanceServiceDto> maintenanceData;
}
