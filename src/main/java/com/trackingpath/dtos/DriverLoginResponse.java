package com.trackingpath.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLoginResponse {
    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private long  driverId;
    private String username;
   
}
