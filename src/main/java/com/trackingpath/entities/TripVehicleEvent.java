package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "trip_vehicle_event")
public class TripVehicleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_execution_id", nullable = false)
    private Long tripExecutionId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "old_vehicle_id")
    private Long oldVehicleId;

    @Column(name = "new_vehicle_id")
    private Long newVehicleId;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(length = 255)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private Long createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}