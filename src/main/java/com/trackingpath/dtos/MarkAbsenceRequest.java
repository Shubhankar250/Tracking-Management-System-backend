package com.trackingpath.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkAbsenceRequest {
    @NotNull
    private Long passengerId;
    @NotBlank
    private String tripDate;
    @NotBlank
    private String tripType;
    private String reason;
}
