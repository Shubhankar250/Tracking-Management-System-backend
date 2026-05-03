package com.trackingpath.dtos;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TerminalParamsDto {

    private String sim;
    private Long deviceId;
    private Integer replySerial;
    private Integer count;

    private List<String> params;

    private Map<String, Object> decoded;

    // Optional (if you want filtering by channelId)
    private Integer channel;
}