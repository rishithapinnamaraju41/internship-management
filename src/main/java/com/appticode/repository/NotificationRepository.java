package com.appticode.repository;

import com.appticode.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false")
    List<Notification> findByUserIdAndIsReadFalse(@Param("userId") String userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId")
    List<Notification> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId")
    Page<Notification> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    List<Notification> findByType(String type);
    
    List<Notification> findByIsReadFalse();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false")
    Long countUnreadNotifications(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") String notificationId);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :date AND n.isRead = true")
    List<Notification> findOldReadNotifications(@Param("date") LocalDateTime date);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date AND n.isRead = true")
    void deleteOldReadNotifications(@Param("date") LocalDateTime date);
}