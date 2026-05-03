package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandDTO {
	private Long id;
	private String model;
    private String commandName;
    private String commandCode;
    private Short commandStatus;
    private String types; 
    private long deviceId;
    private String deviceName;
    private String commandMsg;
    private String created_on;
    private String deviceTimezone;
    private String commandCategory;
    private String commandSubCategory;
    // 🔥 REQUIRED FOR JPQL PROJECTION
    public CommandDTO(
            String deviceName,
            String commandName,
            String commandMsg,
            LocalDateTime createdOn,
            String deviceTimezone,
            String commandCategory,
            String commandSubCategory,
            Long deviceId
    ) {
        this.deviceName = deviceName;
        this.commandName = commandName;
        this.commandMsg = commandMsg;
        this.created_on = createdOn != null ? createdOn.toString() : null;
        this.deviceTimezone=deviceTimezone;
        this.commandCategory = commandCategory;
        this.commandSubCategory = commandSubCategory;
        this.deviceId = deviceId;
    }
    public CommandDTO(
            String deviceName,
            String commandName,
            String commandMsg,
            LocalDateTime createdOn,
            String deviceTimezone,
            String commandCategory,
            String commandSubCategory
    ) {
        this.deviceName = deviceName;
        this.commandName = commandName;
        this.commandMsg = commandMsg;
        this.created_on = createdOn != null ? createdOn.toString() : null;
        this.deviceTimezone=deviceTimezone;
        this.commandCategory = commandCategory;
        this.commandSubCategory = commandSubCategory;
    }
}
