package com.trackingpath.dtos;

import lombok.Data;

@Data
public class UpdateParentProfileRequest {
    private String name;
    private String mobile;
    private String email;
}
