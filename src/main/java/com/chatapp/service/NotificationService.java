package com.chatapp.service;

import com.chatapp.model.Notification;
import com.chatapp.model.User;
import com.chatapp.model.dto.ChatDto;
import com.chatapp.model.dto.MessageDto;
import com.chatapp.model.dto.NotificationDto;
import com.chatapp.model.dto.UserDto;
import com.chatapp.repository.ChatRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.NotificationRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Page<NotificationDto> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(this::convertToDto);
    }

    public List<NotificationDto> findUnreadByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
        
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Long countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    @Transactional
    public NotificationDto createNotification(Long userId, Notification.NotificationType type, 
                                              String title, String content, 
                                              Long relatedUserId, Long relatedChatId, Long relatedMessageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(false);
        
        if (relatedUserId != null) {
            userRepository.findById(relatedUserId).ifPresent(notification::setRelatedUser);
        }
        
        if (relatedChatId != null) {
            chatRepository.findById(relatedChatId).ifPresent(notification::setRelatedChat);
        }
        
        if (relatedMessageId != null) {
            messageRepository.findById(relatedMessageId).ifPresent(notification::setRelatedMessage);
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        return convertToDto(savedNotification);
    }

    @Transactional
    public NotificationDto markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
        
        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own this notification");
        }
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        return convertToDto(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
        
        for (Notification notification : notifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
        
        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own this notification");
        }
        
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    public NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setRead(notification.isRead());
        
        if (notification.getReadAt() != null) {
            dto.setReadAt(notification.getReadAt().format(formatter));
        }
        
        if (notification.getRelatedUser() != null) {
            UserDto userDto = userService.convertToDto(notification.getRelatedUser());
            dto.setRelatedUser(userDto);
        }
        
        if (notification.getRelatedChat() != null) {
            ChatDto chatDto = chatService.convertToDto(notification.getRelatedChat(), notification.getUser().getId());
            dto.setRelatedChat(chatDto);
        }
        
        if (notification.getRelatedMessage() != null) {
            MessageDto messageDto = messageService.convertToDto(notification.getRelatedMessage());
            dto.setRelatedMessage(messageDto);
        }
        
        if (notification.getCreatedAt() != null) {
            dto.setCreatedAt(notification.getCreatedAt().format(formatter));
        }
        
        return dto;
    }
}