package com.trackingpath.services;

import com.trackingpath.dtos.*;

public interface ParentNotificationService {
    NotificationListResponse getNotifications(Long passengerId, Integer page, Integer size);
    ParentNotificationDto getNotification(Long notificationId);
    long markRead(MarkNotificationsReadRequest request);
    void markAllRead();
    NotificationPreferenceResponse getPreferences();
    void savePreferences(NotificationPreferenceRequest request);
}
