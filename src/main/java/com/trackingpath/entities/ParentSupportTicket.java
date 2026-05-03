package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_support_ticket")
@Getter
@Setter
public class ParentSupportTicket {
    @Id
    private Long id;
    private Long parentId;
    private Long passengerId;
    private String subject;
    @Column(length = 5000)
    private String message;
    private String status;
}
