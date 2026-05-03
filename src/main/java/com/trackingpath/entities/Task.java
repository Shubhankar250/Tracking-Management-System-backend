package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long objectId;   // device id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectId", referencedColumnName = "id", insertable = false, updatable = false)
    private DeviceEntity device;

    private String priority;

    private String status;

    private String description;

    private String pickupAddress;

    private String deliveryAddress;

    private Timestamp pickupStartTime;

    private Timestamp pickupEndTime;

    private Timestamp deliveryStartTime;

    private Timestamp deliveryEndTime;

    private Timestamp pickedUpTime;

    private Timestamp deliveredTime;

    private Double pickupLatitude;

    private Double pickupLongitude;

    private Double deliveryLatitude;

    private Double deliveryLongitude;

    private String deliveryImage;
    private String pickupImage;
    private String username;

    private Long userId;
}
