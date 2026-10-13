package com.appticode.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String notificationId; // Format: NOTIF001, NOTIF002 etc.
    
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private User user;
    
    private String title;
    private String message;
    private String type; // INFO, WARNING, SUCCESS, ERROR
    
    private Boolean isRead = false;
    private LocalDateTime readAt;
    
    private String actionUrl;
    private String referenceId;
    private String referenceType;
    
    private LocalDateTime createdAt;
}
