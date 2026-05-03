package com.trackingpath.entities;


import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.trackingpath.dtos.CalibrationDetailDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_sensor_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSensorMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long sensorTypeId;
    private String parameter;
    private String type;
    private String unitOfMeasurement;
    private String ifSensor1;
    private String ifSensor0;
    private String formula;

    private Double lowestValue;
    private Double highestValue;
    private Boolean ignoreIgnitionOff;

    private Long deviceId;

    private Long userId;
    private Long adminId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<CalibrationDetailDTO> calibrationData;
}
