package com.trackingpath.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String format;
    private String period;

    @Column(columnDefinition = "TEXT")
    private String devices;     // comma-separated

    @Column(columnDefinition = "TEXT")
    private String geofences;   // comma-separated

    private String emails;
    private String speed_limit;
    private String stops;
    private String daily;
    private String weekly;
    private String monthly;

    @Column(columnDefinition = "TEXT")
    private String skip_column;

    @Column(name = "admin_id")
    private Long adminId;  // Java camelCase
    @Column(name = "user_id")
    private Long userId;

    private String from_date;
    private String to_date;
}