package com.appticode.controller;

import com.appticode.entity.Notification;
import com.appticode.entity.User;
import com.appticode.service.AuthService;
import com.appticode.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Notification notification) {

        Map<String, Object> response = new HashMap<>();

        try {

            if (notification.getUser() == null || notification.getUser().getUserId() == null) {
                response.put("success", false);
                response.put("message", "User information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            User user = authService.getUserById(notification.getUser().getUserId());

            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Notification title is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Notification message is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Set the fetched user
            notification.setUser(user);

            Notification createdNotification = notificationService.sendNotification(notification);

            response.put("success", true);
            response.put("message", "Notification sent successfully");
            response.put("data", createdNotification);
            response.put("statusCode", HttpStatus.CREATED.value());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", "Failed to send notification: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkNotification(@RequestBody List<Notification> notifications) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (notifications == null || notifications.isEmpty()) {
                response.put("success", false);
                response.put("message", "Notifications list cannot be empty");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            for (Notification notification : notifications) {

                if (notification.getUser() == null || notification.getUser().getUserId() == null) {
                    response.put("success", false);
                    response.put("message", "User information is required for all notifications");
                    response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                User user = authService.getUserById(notification.getUser().getUserId());

                if (user == null) {
                    response.put("success", false);
                    response.put("message", "User not found for userId: " + notification.getUser().getUserId());
                    response.put("statusCode", HttpStatus.NOT_FOUND.value());
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }

                notification.setUser(user);
            }
            
            List<Notification> createdNotifications = notificationService.sendBulkNotification(notifications);
            
            response.put("success", true);
            response.put("message", "Bulk notifications sent successfully");
            response.put("data", createdNotifications);
            response.put("totalCount", createdNotifications.size());
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send bulk notifications: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<Map<String, Object>> getMyNotifications() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentUserId = getCurrentUserId();
            List<Notification> notifications = notificationService.getUserNotifications(currentUserId);
            
            response.put("success", true);
            response.put("message", "Notifications retrieved successfully");
            response.put("data", notifications);
            response.put("totalCount", notifications.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve notifications: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-notifications/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentUserId = getCurrentUserId();
            List<Notification> notifications = notificationService.getUnreadNotifications(currentUserId);
            
            response.put("success", true);
            response.put("message", "Unread notifications retrieved successfully");
            response.put("data", notifications);
            response.put("totalCount", notifications.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve unread notifications: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentUserId = getCurrentUserId();
            Long count = notificationService.getUnreadCount(currentUserId);
            
            response.put("success", true);
            response.put("message", "Unread count retrieved successfully");
            response.put("unreadCount", count);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get unread count: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable String notificationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(notificationId);
            
            if (notificationOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Notification retrieved successfully");
                response.put("data", notificationOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Notification not found with id: " + notificationId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve notification: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean marked = notificationService.markAsRead(notificationId);
            
            if (marked) {
                response.put("success", true);
                response.put("message", "Notification marked as read");
                response.put("notificationId", notificationId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Notification not found with id: " + notificationId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to mark notification as read: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentUserId = getCurrentUserId();
            boolean marked = notificationService.markAllAsRead(currentUserId);
            
            if (marked) {
                response.put("success", true);
                response.put("message", "All notifications marked as read");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to mark notifications as read");
                response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to mark all notifications as read: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable String notificationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = notificationService.deleteNotification(notificationId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Notification deleted successfully");
                response.put("notificationId", notificationId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Notification not found with id: " + notificationId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete notification: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(@PathVariable String type) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(type);
            response.put("success", true);
            response.put("message", "Notifications retrieved successfully for type: " + type);
            response.put("data", notifications);
            response.put("totalCount", notifications.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve notifications: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getNotificationsSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentUserId = getCurrentUserId();
            List<Notification> allNotifications = notificationService.getUserNotifications(currentUserId);
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUserId);
            
            long totalNotifications = allNotifications != null ? allNotifications.size() : 0;
            long unreadCount = unreadNotifications != null ? unreadNotifications.size() : 0;
            long readCount = totalNotifications - unreadCount;
            
            response.put("success", true);
            response.put("message", "Notifications summary retrieved successfully");
            response.put("totalNotifications", totalNotifications);
            response.put("unreadCount", unreadCount);
            response.put("readCount", readCount);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get notifications summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getCurrentUserId() {
        // TODO: Implement this method to get the current logged-in user's ID from security context
        // For now, return a placeholder
        return "USE001";
    }
}