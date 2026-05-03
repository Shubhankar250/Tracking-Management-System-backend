package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_groups")
@Data
public class RouteGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255)
    private String name;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private long adminId;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;
}
