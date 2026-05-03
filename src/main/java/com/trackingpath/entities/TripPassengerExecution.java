package com.trackingpath.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "trip_passenger_execution")
public class TripPassengerExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATIONS =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_execution_id", nullable = false)
    private TripExecution tripExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_stop_execution_id")
    private TripStopExecution tripStopExecution;

    // ================= BASIC INFO =================

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "passenger_name_snapshot", nullable = false, length = 150)
    private String passengerNameSnapshot;

    @Column(name = "passenger_code_snapshot", length = 100)
    private String passengerCodeSnapshot;

    @Column(name = "guardian_name_snapshot", length = 150)
    private String guardianNameSnapshot;

    @Column(name = "guardian_mobile_snapshot", length = 20)
    private String guardianMobileSnapshot;

    // ================= STOP INFO =================

    private Long pickupStopId;
    private String pickupStopNameSnapshot;

    private Long dropStopId;
    private String dropStopNameSnapshot;

    // ================= STATUS =================

    @Column(nullable = false, length = 30)
    private String attendanceStatus;

    @Column(nullable = false, length = 30)
    private String boardingStatus;

    @Column(nullable = false, length = 30)
    private String deboardingStatus;

    // ================= ATTENDANCE =================

    private String attendanceMarkSource;
    private LocalDateTime attendanceMarkTime;

    // ================= BOARDING =================

    private LocalDateTime boardTime;
    private String boardSource;
    private Long boardVehicleId;

    // ================= DEBOARDING =================

    private LocalDateTime deboardTime;
    private String deboardSource;
    private Long deboardVehicleId;
    private String  class_name;
    private Long role_number;
    // ================= EXTRA =================

    private String rfidCode;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(name = "guardian_informed")
    private Boolean guardianInformed;

    @Column(name = "guardian_verified")
    private Boolean guardianVerified;

    @Column(name = "guardian_verified_at")
    private LocalDateTime guardianVerifiedAt;

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

    // ================= GETTERS / SETTERS =================

}