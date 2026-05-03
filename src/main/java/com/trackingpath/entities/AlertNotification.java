
package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ignoreNotification;
    private String soundNotification;
    private String popupNotification;
    private Boolean appPushNotification;
    private String emailNotification;
    private String webhookNotification;
    private String notificationColor;

    private Long userId;
    private Long adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;
}

