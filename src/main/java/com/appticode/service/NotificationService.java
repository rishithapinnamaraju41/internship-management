package com.appticode.service;

import com.appticode.entity.Notification;
import com.appticode.repository.NotificationRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Notification sendNotification(Notification notification) {
        notification.setNotificationId(idGenerator.generateNotificationId());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public List<Notification> sendBulkNotification(List<Notification> notifications) {
        for (Notification notification : notifications) {
            notification.setNotificationId(idGenerator.generateNotificationId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
        }
        return notificationRepository.saveAll(notifications);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public Long getUnreadCount(String userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    public Optional<Notification> getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    public boolean markAsRead(String notificationId) {
        try {
            notificationRepository.markAsRead(notificationId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean markAllAsRead(String userId) {
        try {
            notificationRepository.markAllAsRead(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteNotification(String notificationId) {
        try {
            if (notificationRepository.existsById(notificationId)) {
                notificationRepository.deleteById(notificationId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteOldReadNotifications(LocalDateTime date) {
        try {
            notificationRepository.deleteOldReadNotifications(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Notification> findOldReadNotifications(LocalDateTime date) {
        return notificationRepository.findOldReadNotifications(date);
    }

    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findByIsReadFalse() {
        return notificationRepository.findByIsReadFalse();
    }

    public boolean existsByNotificationId(String notificationId) {
        return notificationRepository.existsById(notificationId);
    }
}