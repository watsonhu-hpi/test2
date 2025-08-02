package com.chatapp.controller;

import com.chatapp.model.dto.NotificationDto;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Page<NotificationDto> notifications = notificationService.findByUserId(userDetails.getId(), page, size);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<NotificationDto> notifications = notificationService.findUnreadByUserId(userDetails.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long count = notificationService.countUnreadByUserId(userDetails.getId());
        
        return ResponseEntity.ok(new HashMap<String, Long>() {{
            put("count", count);
        }});
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        NotificationDto notificationDto = notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(notificationDto);
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.markAllAsRead(userDetails.getId());
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "All notifications marked as read");
        }});
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.deleteNotification(id, userDetails.getId());
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Notification deleted successfully");
        }});
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAllNotifications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        notificationService.deleteAllNotifications(userDetails.getId());
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "All notifications deleted successfully");
        }});
    }
}