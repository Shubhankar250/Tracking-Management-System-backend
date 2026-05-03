package com.trackingpath.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutesDTO {

    private Long id;
    private String name;
    private String description;
    private String group;

    private Double buffer;

    private String geom;       // GeoJSON String
    private String bufferGeom; // GeoJSON String
}
