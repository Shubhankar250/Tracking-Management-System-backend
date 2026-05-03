package com.trackingpath.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoiDto {
    private Long id;
    private String name;
    private String description;
    private Integer poiGroupId; // group id
    private String poiGroupName;
    private String markerIcon;
    private Double radius;
    private Double latitude;
    private Double longitude;
}
