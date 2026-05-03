package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "alert_geofence_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertGeofenceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long geofenceId;

    private String geofenceInOut;

    private Long userId;

    private Long adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;
}

