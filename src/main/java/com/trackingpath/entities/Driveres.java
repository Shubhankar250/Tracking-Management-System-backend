package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "driveres")
@Data
@NoArgsConstructor
public class Driveres {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private DeviceEntity device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_device_id")
    private DeviceEntity currentDevice; 

    private String rfid;
    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    private String username;
    private String password;
    public Driveres(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
