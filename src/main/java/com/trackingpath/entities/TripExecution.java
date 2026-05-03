package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "trip_execution")
public class TripExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "route_name_snapshot", nullable = false, length = 150)
    private String routeNameSnapshot;

    @Column(name = "route_type", nullable = false, length = 20)
    private String routeType;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "shift_name_snapshot", nullable = false, length = 100)
    private String shiftNameSnapshot;

    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    private Long plannedVehicleId;
    private Long actualVehicleId;

    private Long driverId;
    private Long attendantId;

    private Boolean vehicleReplaced = false;
    private Integer replacementCount = 0;

    private Integer totalStops = 0;
    private Integer coveredStops = 0;
    @Column(precision = 5, scale = 2)
    private BigDecimal stopCoveragePercent = BigDecimal.ZERO;

    private Integer totalPassengersAssigned = 0;
    private Integer totalPresent = 0;
    private Integer totalAbsent = 0;
    private Integer totalBoarded = 0;
    private Integer totalDeboarded = 0;

    @Column(nullable = false, length = 30)
    private String tripStatus;

    @Column(length = 255)
    private String statusReason;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================== Lifecycle Hooks ==================

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

   
}