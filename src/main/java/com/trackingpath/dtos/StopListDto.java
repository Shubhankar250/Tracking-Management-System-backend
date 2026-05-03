package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopListDto {

    private Integer sequence;
    private String stopName;
    private String etaTime;
    private String status;
    private long boarded;
    
}