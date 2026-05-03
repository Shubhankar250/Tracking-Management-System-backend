package com.trackingpath.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "livedata")
@Data
public class LiveData {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   
    
    @OneToOne
    @JoinColumn(name = "deviceid", nullable = false)
    private DeviceEntity device;
   


    
    private String protocol;

    private LocalDateTime servertime;
    private LocalDateTime devicetime;
    private LocalDateTime fixtime;

    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double course;

    @Column(length = 512)
    private String address;

    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes;
    private String network;
    private String accuracy;
    private Double fuellevel;

    private LocalDateTime lastidletime;
    private Boolean valid;
    private LocalDateTime lastmovementtime;
    
}

