package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_absence_request")
@Getter
@Setter
public class ParentAbsenceRequest {
    @Id
    private Long id;
    private Long parentId;
    private Long passengerId;
    private String tripDate;
    private String tripType;
    @Column(length = 1000)
    private String reason;
}
