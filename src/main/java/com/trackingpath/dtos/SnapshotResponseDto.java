package com.trackingpath.dtos;

import java.util.List;

import lombok.Data;

@Data
public class SnapshotResponseDto {

    private String sim;
    private Long deviceId;
    private Integer respSerialNo;
    private Integer result;
    private List<Long> mediaIds;
}
