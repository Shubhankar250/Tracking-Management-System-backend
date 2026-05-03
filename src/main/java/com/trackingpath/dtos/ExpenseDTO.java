package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class ExpenseDTO {


    private Long id;
    private String expenseName;
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

    private Long deviceId;
    private String deviceName;

    public ExpenseDTO(
    	    Long id,
    	    String expenseName,
    	    String date,
    	    Long expenseOdometer,
    	    Double cost,
    	    String description,
    	    String supplier,
    	    String buyer,
    	    Long quantity,
    	    Long engineHour,
    	    Long adminId,
    	    Long userId,
    	    Long deviceId,
    	    String deviceName
    	) {
    	    this.id = id;
    	    this.expenseName = expenseName;
    	    this.date = date;
    	    this.expenseOdometer = expenseOdometer;
    	    this.cost = cost;
    	    this.description = description;
    	    this.supplier = supplier;
    	    this.buyer = buyer;
    	    this.quantity = quantity;
    	    this.engineHour = engineHour;
    	    this.adminId = adminId;
    	    this.userId = userId;
    	    this.deviceId = deviceId;
    	    this.deviceName = deviceName;
    	}

}
