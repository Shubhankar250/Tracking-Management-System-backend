package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupDeviceDTO {

    private long id;
    private String vehicle_status;
    private String name;
    private String uniqueid;

}

