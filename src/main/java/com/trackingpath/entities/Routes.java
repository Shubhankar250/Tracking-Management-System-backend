package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "group")
    private String group;

    @Column(name = "buffer_distance")
    private Double buffer;

    @Column(name = "geom", columnDefinition = "geometry")
    private Geometry geom;

    @Column(name = "buffer_geom", columnDefinition = "geometry")
    private Geometry bufferGeom;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "user_id")
    private Long userId;
    
    public Routes(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
}

