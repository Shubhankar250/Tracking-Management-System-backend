package com.trackingpath.dtos;

import java.util.Date;
import lombok.Data;
@Data
public class SoftwareReleaseDTO {

    private Long id;
    private Date date;
    private String text;
    private Long userId;

}