package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_notification_preference")
@Getter
@Setter
public class ParentNotificationPreference {
    @Id
    private Long id;
    private Long parentId;
    private boolean busReachedStopAlert;
    private boolean childBoardedAlert;
    private boolean schoolArrivalAlert;
    private boolean dropHandoverAlert;
    private boolean schoolCirculars;
    private boolean promotionalAlerts;
}
