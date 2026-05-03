package com.trackingpath.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transport_route")
@Getter
@Setter
public class TransportRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false, length = 150)
    private String routeName;

    @Column(name = "route_type", nullable = false, length = 20)
    private String routeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "default_vehicle_id")
    private Long defaultVehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_vehicle_id", insertable = false, updatable = false)
    private DeviceEntity defaultVehicle;

    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "route_geojson", columnDefinition = "TEXT")
    private String routeGeoJson;
    
    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "attendant_id")
    private Long attendantId;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private TransportShift shift;
}
