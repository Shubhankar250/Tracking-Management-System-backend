package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String protocol;

    @Column(name = "deviceid", nullable = false)
    private Integer deviceId;

    @Column(name = "servertime", nullable = false)
    private LocalDateTime serverTime;

    @Column(name = "devicetime", nullable = false)
    private LocalDateTime deviceTime;

    @Column(name = "fixtime", nullable = false)
    private LocalDateTime fixTime;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double altitude;

    @Column(nullable = false)
    private Double speed;

    @Column(nullable = false)
    private Double course;

    private String address;

    @Column(nullable = false, length = 10000)
    private String attributes;
    
    @Column(name = "network", columnDefinition = "TEXT")
    private String network;

    private String accuracy;

    @Column(name = "fuellevel")
    private Double fuelLevel;

    private Boolean valid;
}