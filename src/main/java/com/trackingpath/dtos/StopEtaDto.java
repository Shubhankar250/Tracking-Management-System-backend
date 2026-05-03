package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopEtaDto {
    private Long stopId;
    private String stopName;
    private Integer sequenceNo;
    private String status;
    private String eta;
}
