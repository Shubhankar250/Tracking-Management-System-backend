package com.trackingpath.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardingInfo {
    private String boardingStatus;
    private LocalDateTime boardTime;
}