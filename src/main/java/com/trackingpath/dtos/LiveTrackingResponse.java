package com.trackingpath.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTrackingResponse {
    private Long tripId;
    private Long routeId;
    private Long vehicleId;
    private String vehicleNumber;
    private String tripStatus;
    private LocationDto currentLocation;
    private Integer speedKph;
    private String lastUpdateTime;
    private String schoolEta;
    private String routeGeoJson;
    private List<StopEtaDto> stops;
}
