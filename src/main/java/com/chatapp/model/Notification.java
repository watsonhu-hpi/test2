package com.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_user_id")
    private User relatedUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_chat_id")
    private Chat relatedChat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_message_id")
    private Message relatedMessage;
    
    private boolean read;
    private LocalDateTime readAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        MESSAGE,
        FRIEND_REQUEST,
        FRIEND_ACCEPT,
        GROUP_INVITATION,
        GROUP_JOIN,
        MENTION,
        REACTION,
        SYSTEM
    }
}