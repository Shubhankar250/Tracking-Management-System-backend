package com.trackingpath.responses;

import lombok.Data;

@Data
public class ExternalLoginResponse {
    private String token;
    private long expiresIn;
}

