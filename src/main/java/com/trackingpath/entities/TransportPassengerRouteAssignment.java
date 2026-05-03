package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_passenger_route_assignment")
@Getter
@Setter
public class TransportPassengerRouteAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private TransportPassenger passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private TransportRoute route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_stop_id")
    private TransportRouteStop pickupStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_stop_id")
    private TransportRouteStop dropStop;

    @Column(name = "auto_login_enabled", nullable = false)
    private Boolean autoLoginEnabled = false;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "temp_password", length = 200)
    private String tempPassword;

    @Column(name = "password_changed")
    private Boolean passwordChanged;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}
