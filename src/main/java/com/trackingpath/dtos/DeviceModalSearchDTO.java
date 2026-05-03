package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceModalSearchDTO {

    private Long id;
    private String modalName;
    private String image;

}