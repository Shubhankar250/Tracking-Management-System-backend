package com.trackingpath.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiveStopDto {

    private Integer sequence;
    private String stopName;
    private Double latitude;
    private Double longitude;

    private String status;
    private Integer boarded;
    private String announcementFile;
    private String eta;
}
