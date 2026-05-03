package com.trackingpath.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "sensor_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_type_name")
    private String sensorTypeName;

    @Column(name = "calibration_required")
    private Boolean calibrationRequired;
    @Column(name = "type") 
    private String type;
}
