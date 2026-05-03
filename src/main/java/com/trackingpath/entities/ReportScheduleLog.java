package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_schedule_log")
@Getter
@Setter
public class ReportScheduleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long scheduleId;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(length = 20)
    private String status;

    private Integer recipientCount;

    @Column(columnDefinition = "text")
    private String filePath;

    @Column(columnDefinition = "text")
    private String errorMessage;
    @Column(name = "file_size")
    private String fileSize;
}


