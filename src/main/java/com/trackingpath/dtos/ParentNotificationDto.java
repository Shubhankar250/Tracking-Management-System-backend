package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentNotificationDto {
    private Long notificationId;
    private String type;
    private String title;
    private String message;
    private String createdAt;
    private boolean read;
}
