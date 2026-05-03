package com.trackingpath.dtos;

import lombok.Data;

@Data
public class SnapshotFileDto {

    private String sim;
    private Long deviceId;
    private Long mediaId;
    private Integer mediaType;
    private Integer format;
    private Integer eventCode;
    private Integer channel;
    private Long size;
    private String file;
    private Boolean normalized;
    private Integer width;
    private Integer height;
}