package com.trackingpath.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
    
    @NotBlank
    private String role;
    
}
