package com.trackingpath.dtos;

import lombok.Data;

@Data
public class ExternalLoginRequest {
    private String email;
    private String password;
}