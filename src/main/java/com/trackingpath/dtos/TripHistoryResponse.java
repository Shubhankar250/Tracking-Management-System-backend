package com.trackingpath.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripHistoryResponse {
    private Long passengerId;
    private List<TripHistoryItemDto> history;
    private Integer page;
    private Integer size;
    private Long total;
}
