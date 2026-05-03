package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDataDTO {

    private Long id;
    private String alertName;
    private String alertType;
    private String status;
    private Long devicesCount;
}

