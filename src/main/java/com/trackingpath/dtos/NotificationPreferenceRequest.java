package com.trackingpath.dtos;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private boolean busReachedStopAlert;
    private boolean childBoardedAlert;
    private boolean schoolArrivalAlert;
    private boolean dropHandoverAlert;
    private boolean schoolCirculars;
    private boolean promotionalAlerts;
}
