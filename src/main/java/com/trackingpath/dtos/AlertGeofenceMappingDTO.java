package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertGeofenceMappingDTO {

    private Long id;
    private Long alertId;

    private List<Long> geofenceIds;

    private String geofenceInOut;
}

