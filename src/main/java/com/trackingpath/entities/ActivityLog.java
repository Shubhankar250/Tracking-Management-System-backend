package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "log_type", length = 50)
    private String logType;

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "http_referal", columnDefinition = "text")
    private String httpReferal;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "user_id")
    private Long userId;
}
