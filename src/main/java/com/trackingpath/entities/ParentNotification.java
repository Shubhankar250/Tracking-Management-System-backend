package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_notification")
@Getter
@Setter
public class ParentNotification {
    @Id
    private Long id;
    private Long parentId;
    private Long passengerId;
    private String type;
    private String title;
    @Column(length = 2000)
    private String message;
    private Boolean readFlag = false;
    private String createdAt;
}
