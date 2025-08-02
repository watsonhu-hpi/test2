package com.chatapp.repository;

import com.chatapp.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read);
    
    Long countByUserIdAndRead(Long userId, boolean read);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = ?1 AND n.type = ?2")
    List<Notification> findByUserIdAndType(Long userId, String notificationType);
    
    @Query("SELECT n FROM Notification n WHERE n.relatedUser.id = ?1")
    List<Notification> findByRelatedUserId(Long relatedUserId);
    
    @Query("SELECT n FROM Notification n WHERE n.relatedChat.id = ?1")
    List<Notification> findByRelatedChatId(Long relatedChatId);
}