package com.trackingpath.entities;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Geometry;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name= "geofence")
@AllArgsConstructor
@NoArgsConstructor
public class Geofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String color;

    @Column(name = "geom", columnDefinition = "geometry", insertable = false, updatable = false)
    private Object geom;

    private String pcts_type;
    private String geo_group;
    private String speed_limit;

    // Changed primitive double -> wrapper Double
    private Double radius;

    private Long admin_id;
    private Long user_id;

    private LocalDateTime creation_time = LocalDateTime.now();

    public Geofence(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
