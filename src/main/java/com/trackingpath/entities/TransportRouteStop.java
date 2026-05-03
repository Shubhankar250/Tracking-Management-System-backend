package com.trackingpath.entities;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transport_route_stop")
@Getter
@Setter
public class TransportRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private TransportRoute route;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "stop_name", nullable = false, length = 150)
    private String stopName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "stop_type", length = 20)
    private String stopType;

    @Column(name = "geofence_radius")
    private Integer geofenceRadius;

    @Column(name = "auto_detected")
    private Boolean autoDetected = false;
    
    @Column(name = "approved")
    private Boolean approved = true;

    @Column(name = "passenger_count")
    private Integer passengerCount = 0;
    @Column(name = "client_stop_id")
    private Long clientStopId;
    @Column(name = "announcement_file", length = 255)
    private String announcementFile;
    
    private LocalTime plannedArrivalTime;
    private LocalTime plannedDepartureTime;
}
