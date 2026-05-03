package com.trackingpath.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveDataResponseDTO {

    private Map<String, Object> attributes;

    private String name;
    private String companyName;
    private String deviceUniqueId;

    private LocalDateTime timestamp;
    private LocalDateTime serverTime;
    private LocalDateTime deviceTime;
    private LocalDateTime fixTime;
    private LocalDateTime lastStatusUpdate;

    private Boolean valid;

    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double course;

    private String address;
    private Double accuracy;
} 