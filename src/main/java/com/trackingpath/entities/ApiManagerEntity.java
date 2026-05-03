package com.trackingpath.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_manager")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiManagerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean status;

    private String name;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "valid_time")
    private String validTime;

    private String email;
    private String phone;

    @Column(name = "url")
    private String baseUrl;

    @Column(name = "delete_after_expiration")
    private boolean deleteAfterExpiration;

    @Column(name = "access_code")
    private Long accessCode;

    @Column(name = "access_start_time")
    private String accessStartTime;

    @Column(name = "access_end_time")
    private String accessEndTime;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;
    
   
    
    
}
