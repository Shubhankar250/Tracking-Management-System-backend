package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "expenses")
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expenseName;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deviceId")
	private DeviceEntity device;

    @Transient
    private Long deviceId;

    private String date;
    private Long expenseOdometer;
    private Double cost;

    private String description;
    private String supplier;
    private String buyer;

    private Long quantity;
    private Long engineHour;

    private Long adminId;
    private Long userId;
    
    
}
