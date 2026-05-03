package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "report_schedule")
@Getter
@Setter
public class ReportSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 200)
    private String scheduleName;

    @Column(nullable = false, length = 100)
    private String reportType;

    @Column(nullable = false, columnDefinition = "text")
    private String emailTo;

    @Column(columnDefinition = "text")
    private String emailCc;
    @Column(nullable = false, length = 20)
    private String outputFormat;

    @Column(nullable = false, length = 20)
    private String scheduleType;

    @Column(nullable = true,length = 120)
    private String cronExpression;

    @Column(nullable = false, length = 80)
    private String timezone = "Asia/Kolkata";

    @Column(columnDefinition = "text")
    private String filterJson;

    private Boolean active = true;

    private LocalDateTime oneTimeRunAt;
    private LocalDateTime lastRunAt;
    private LocalDateTime nextRunAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    //new added
    private String title;
    private String period;
    @Column(columnDefinition = "TEXT")
    private String devices;     // comma-separated
    @Column(columnDefinition = "TEXT")
    private String geofences;   // comma-separated
    private String speed_limit;
    private String stops;
    private String daily;
    private String weekly;
    private String monthly;
    @Column(columnDefinition = "TEXT")
    private String skip_column;
    private String from_date;
    private String to_date;
    private String subject;
    private String email_body;
    private Long template_id;


    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.timezone == null || this.timezone.isBlank()) {
            this.timezone = "Asia/Kolkata";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
