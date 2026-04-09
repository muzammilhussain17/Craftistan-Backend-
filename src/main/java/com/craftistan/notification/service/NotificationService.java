package com.craftistan.notification.service;

import com.craftistan.notification.entity.Notification;
import com.craftistan.notification.repository.NotificationRepository;
import com.craftistan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create a new notification for a user
     */
    @Transactional
    public void createNotification(String userId, String type, String title, String message, String targetId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .targetId(targetId)
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Get paginated notifications for the current user
     */
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * Get unread notification count
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    /**
     * Mark a specific notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            // Ensure the notification belongs to this user before marking read
            if (notification.getUserId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    /**
     * Mark all unread notifications for a user as read
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findAllByUserIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(user.getId())) {
                notificationRepository.delete(notification);
            }
        });
    }
}
