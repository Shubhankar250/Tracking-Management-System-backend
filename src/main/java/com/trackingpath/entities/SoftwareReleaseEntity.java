package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "software_release")
public class SoftwareReleaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "release_date")
    private Date date;

    @Column(name = "release_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "user_id")
    private Long userId;

}