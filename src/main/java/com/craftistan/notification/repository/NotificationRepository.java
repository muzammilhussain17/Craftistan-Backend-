package com.craftistan.notification.repository;

import com.craftistan.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Fetch notifications for a specific user, ordered by most recent
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Fetch unread notifications for a specific user
    List<Notification> findByUserIdAndIsReadFalse(String userId);

    // Count unread notifications
    long countByUserIdAndIsReadFalse(String userId);
    
    // Find all unread
    List<Notification> findAllByUserIdAndIsReadFalse(String userId);
}
