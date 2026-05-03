package com.trackingpath.dtos;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogDTO {

    private Long id;
    private Long userId;
    private String createdBy;

    private String activityType;
    private String message;
    private String activityTime;

    private String userAgent;
    private String ipAddress;
    private String httpReferal;
}

