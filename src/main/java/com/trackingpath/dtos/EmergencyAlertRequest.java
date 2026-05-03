package com.trackingpath.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyAlertRequest {
    @NotNull
    private Long passengerId;
    @NotNull
    private Long tripId;
    @NotBlank
    private String alertType;
    private String message;
}
