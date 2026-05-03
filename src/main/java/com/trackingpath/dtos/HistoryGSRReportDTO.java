package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryGSRReportDTO {

    private String imei;
    private String name;
    private List<RoutePoint> route;
    private List<StopInfo> stops;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoutePoint {
        private double lat;
        private double lon;
        private String deviceTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StopInfo {
        private String startTime;
        private String endTime;
        private String duration;
    }
}