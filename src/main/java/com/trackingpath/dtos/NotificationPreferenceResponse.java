package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    private Long parentId;
    private boolean busReachedStopAlert;
    private boolean childBoardedAlert;
    private boolean schoolArrivalAlert;
    private boolean dropHandoverAlert;
    private boolean schoolCirculars;
    private boolean promotionalAlerts;
}
