package com.chatapp.controller;

import com.chatapp.model.dto.MessageDto;
import com.chatapp.model.dto.MessageRequest;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.ChatService;
import com.chatapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/chat/{chatId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<MessageDto>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), chatId)) {
            return ResponseEntity.status(403).build();
        }
        
        Page<MessageDto> messages = messageService.findByChatId(chatId, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageDto> getMessageById(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto messageDto = messageService.findById(id);
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), messageDto.getChatId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(messageDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageDto> createMessage(
            @Valid @RequestBody MessageRequest messageRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), messageRequest.getChatId())) {
            return ResponseEntity.status(403).build();
        }
        
        MessageDto createdMessage = messageService.createMessage(messageRequest, userDetails.getId());
        return ResponseEntity.ok(createdMessage);
    }

    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addAttachmentToMessage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user is the sender of this message
        if (!message.getSender().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        String fileUrl = messageService.addAttachment(id, file);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("url", fileUrl);
        }});
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user is the sender of this message
        if (!message.getSender().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        MessageDto updatedMessage = messageService.updateMessage(id, updates);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user is the sender of this message or a chat admin
        if (!(message.getSender().getId().equals(userDetails.getId()) || 
              chatService.isUserChatCreator(userDetails.getId(), message.getChatId()))) {
            return ResponseEntity.status(403).build();
        }
        
        messageService.deleteMessage(id);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Message deleted successfully");
        }});
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), message.getChatId())) {
            return ResponseEntity.status(403).build();
        }
        
        messageService.markAsRead(id, userDetails.getId());
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Message marked as read");
        }});
    }

    @PostMapping("/{id}/reaction")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> reactionRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), message.getChatId())) {
            return ResponseEntity.status(403).build();
        }
        
        messageService.addReaction(id, userDetails.getId(), reactionRequest.get("emoji"));
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Reaction added successfully");
        }});
    }

    @DeleteMapping("/{id}/reaction")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> removeReaction(
            @PathVariable Long id,
            @RequestParam String emoji,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MessageDto message = messageService.findById(id);
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), message.getChatId())) {
            return ResponseEntity.status(403).build();
        }
        
        messageService.removeReaction(id, userDetails.getId(), emoji);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Reaction removed successfully");
        }});
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<MessageDto>> searchMessages(
            @RequestParam String query,
            @RequestParam(required = false) Long chatId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<MessageDto> messages;
        if (chatId != null) {
            // Verify user has access to this chat
            if (!chatService.isUserInChat(userDetails.getId(), chatId)) {
                return ResponseEntity.status(403).build();
            }
            messages = messageService.searchMessagesInChat(query, chatId);
        } else {
            messages = messageService.searchMessagesForUser(query, userDetails.getId());
        }
        
        return ResponseEntity.ok(messages);
    }
}