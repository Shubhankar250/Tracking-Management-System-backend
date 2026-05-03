package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SchoolNotificationDTO {

private String passengername;
private LocalDateTime boardedtime;
private LocalDateTime Eta;	
}
