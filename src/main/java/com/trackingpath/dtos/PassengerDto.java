package com.trackingpath.dtos;

import lombok.Data;

@Data
public class PassengerDto {
    private Long id;
    private String passengerCode;
    private String passengerName;
    private String passengerType;
    private String guardianName;
    private String guardianMobile;
    private String guardianEmail;
    private String admissionNo;
    private String className;
    private String sectionName;
    private String gender;
    private Boolean active;
}
