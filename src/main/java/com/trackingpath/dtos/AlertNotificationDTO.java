package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertNotificationDTO {

    private Long id;
    private Long ignoreNotification;

    private String soundNotification;
    private String popupNotification;

    private Boolean appPushNotification;

    private String emailNotification;
    private String webhookNotification;

    private String notificationColor;

    private Long alertId;
}

