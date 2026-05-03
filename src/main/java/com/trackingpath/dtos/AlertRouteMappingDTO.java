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
public class AlertRouteMappingDTO {

    private Long id;
    private Long alertId;

    private List<Long> routeIds;

    private String routeInOut;
}

