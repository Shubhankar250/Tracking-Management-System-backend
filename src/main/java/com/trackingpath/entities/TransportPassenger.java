package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_passenger")
@Getter
@Setter
public class TransportPassenger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_code", length = 50)
    private String passengerCode;

    @Column(name = "passenger_name", nullable = false, length = 150)
    private String passengerName;

    @Column(name = "passenger_type", nullable = false, length = 20)
    private String passengerType = "Student";

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_mobile", length = 20)
    private String guardianMobile;

    @Column(name = "guardian_email", length = 150)
    private String guardianEmail;

    @Column(name = "admission_no", length = 100)
    private String admissionNo;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "section_name", length = 50)
    private String sectionName;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(length = 100)
    private String rfidCode;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
