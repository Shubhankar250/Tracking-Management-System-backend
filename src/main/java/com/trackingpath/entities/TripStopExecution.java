package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "trip_stop_execution")
public class TripStopExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_execution_id", nullable = false)
    private TripExecution tripExecution;

    // ================= BASIC INFO =================

    @Column(name = "route_stop_id")
    private Long routeStopId;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "stop_name_snapshot", nullable = false, length = 150)
    private String stopNameSnapshot;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // ================= TIMINGS =================

    private LocalDateTime plannedArrivalTime;
    private LocalDateTime plannedDepartureTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualDepartureTime;
    @Column(name = "announcement_file")
    private String announcementFile;
    private LocalDateTime etaTime;

    // ================= VEHICLE =================

    private Long vehicleIdAtStop;

    // ================= STATUS =================

    @Column(nullable = false, length = 30)
    private String stopStatus;

    private Boolean isCovered = false;

    // ⚠️ IMPORTANT: numeric(5,2) → BigDecimal (NOT Double)
    @Column(name = "coverage_percent", precision = 5, scale = 2)
    private BigDecimal coveragePercent = BigDecimal.ZERO;

    // ================= PASSENGER COUNTS =================

    private Integer assignedPassengerCount = 0;
    private Integer presentCount = 0;
    private Integer absentCount = 0;
    private Integer boardedCount = 0;
    private Integer deboardedCount = 0;

    // ================= EXTRA =================

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= LIFECYCLE =================

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