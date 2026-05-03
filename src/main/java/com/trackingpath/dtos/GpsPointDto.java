package com.trackingpath.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GpsPointDto {
    private Double latitude;
    private Double longitude;
    private LocalDateTime fixTime;
    private Double speedKph;
}
