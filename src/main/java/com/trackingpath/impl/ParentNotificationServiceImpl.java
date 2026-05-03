package com.trackingpath.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.trackingpath.dtos.*;
import com.trackingpath.entities.ParentNotification;
import com.trackingpath.entities.ParentNotificationPreference;
import com.trackingpath.repositories.ParentNotificationPreferenceRepository;
import com.trackingpath.repositories.ParentNotificationRepository;
import com.trackingpath.services.ParentNotificationService;
import com.trackingpath.util.SecurityUtils;

@Service
public class ParentNotificationServiceImpl implements ParentNotificationService {

    private final ParentNotificationRepository notificationRepository;
    private final ParentNotificationPreferenceRepository preferenceRepository;

    public ParentNotificationServiceImpl(ParentNotificationRepository notificationRepository,
                                         ParentNotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public NotificationListResponse getNotifications(Long passengerId, Integer page, Integer size) {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        List<ParentNotificationDto> items = notificationRepository.findByParentIdOrderByIdDesc(parentId)
                .stream()
                .map(this::toDto)
                .toList();

        return NotificationListResponse.builder()
                .items(items)
                .unreadCount(notificationRepository.countByParentIdAndReadFlagFalse(parentId))
                .build();
    }

    @Override
    public ParentNotificationDto getNotification(Long notificationId) {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        ParentNotification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getParentId().equals(parentId))
                .orElseThrow(() -> new SecurityException("Notification not found"));
        return toDto(notification);
    }

    @Override
    public long markRead(MarkNotificationsReadRequest request) {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        long updated = 0;
        for (Long id : request.getNotificationIds()) {
            ParentNotification notification = notificationRepository.findById(id).orElse(null);
            if (notification != null && notification.getParentId().equals(parentId)) {
                notification.setReadFlag(true);
                notificationRepository.save(notification);
                updated++;
            }
        }
        return updated;
    }

    @Override
    public void markAllRead() {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        List<ParentNotification> notifications = notificationRepository.findByParentIdOrderByIdDesc(parentId);
        notifications.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public NotificationPreferenceResponse getPreferences() {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        ParentNotificationPreference pref = preferenceRepository.findByParentId(parentId)
                .orElse(NotificationPreferencePreferenceDefaults.of(parentId));

        return NotificationPreferenceResponse.builder()
                .parentId(parentId)
                .busReachedStopAlert(pref.isBusReachedStopAlert())
                .childBoardedAlert(pref.isChildBoardedAlert())
                .schoolArrivalAlert(pref.isSchoolArrivalAlert())
                .dropHandoverAlert(pref.isDropHandoverAlert())
                .schoolCirculars(pref.isSchoolCirculars())
                .promotionalAlerts(pref.isPromotionalAlerts())
                .build();
    }

    @Override
    public void savePreferences(NotificationPreferenceRequest request) {
        Long parentId = SecurityUtils.getCurrentUser().getParentId();
        ParentNotificationPreference pref = preferenceRepository.findByParentId(parentId)
                .orElse(new ParentNotificationPreference());

        pref.setParentId(parentId);
        pref.setBusReachedStopAlert(request.isBusReachedStopAlert());
        pref.setChildBoardedAlert(request.isChildBoardedAlert());
        pref.setSchoolArrivalAlert(request.isSchoolArrivalAlert());
        pref.setDropHandoverAlert(request.isDropHandoverAlert());
        pref.setSchoolCirculars(request.isSchoolCirculars());
        pref.setPromotionalAlerts(request.isPromotionalAlerts());
        preferenceRepository.save(pref);
    }

    private ParentNotificationDto toDto(ParentNotification n) {
        return ParentNotificationDto.builder()
                .notificationId(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .createdAt(n.getCreatedAt())
                .read(Boolean.TRUE.equals(n.getReadFlag()))
                .build();
    }

    private static final class NotificationPreferencePreferenceDefaults {
        private NotificationPreferencePreferenceDefaults() {}
        static ParentNotificationPreference of(Long parentId) {
            ParentNotificationPreference p = new ParentNotificationPreference();
            p.setParentId(parentId);
            p.setBusReachedStopAlert(true);
            p.setChildBoardedAlert(true);
            p.setSchoolArrivalAlert(true);
            p.setDropHandoverAlert(true);
            p.setSchoolCirculars(true);
            p.setPromotionalAlerts(false);
            return p;
        }
    }
}
