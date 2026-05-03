package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_emergency_alert")
@Getter
@Setter
public class ParentEmergencyAlert {
    @Id
    private Long id;
    private Long parentId;
    private Long passengerId;
    private Long tripId;
    private String alertType;
    @Column(length = 2000)
    private String message;
}
