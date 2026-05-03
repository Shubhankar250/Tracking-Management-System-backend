package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_poi_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertPoiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poiId;
    private String idleStopPoi;
    private Integer stopDuration;
    private Integer idleDuration;

    private Long userId;
    private Long adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;
}

