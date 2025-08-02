package com.chatapp.controller;

import com.chatapp.model.Chat;
import com.chatapp.model.dto.ChatDto;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatDto>> getUserChats(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ChatDto> chats = chatService.findChatsByUserId(userDetails.getId());
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ChatDto> getChatById(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), id)) {
            return ResponseEntity.status(403).build();
        }
        
        ChatDto chatDto = chatService.findDtoById(id);
        return ResponseEntity.ok(chatDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ChatDto> createChat(
            @Valid @RequestBody Map<String, Object> chatRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ChatDto createdChat = chatService.createChat(chatRequest, userDetails.getId());
        return ResponseEntity.ok(createdChat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ChatDto> updateChat(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), id)) {
            return ResponseEntity.status(403).build();
        }
        
        ChatDto updatedChat = chatService.updateChat(id, updates);
        return ResponseEntity.ok(updatedChat);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteChat(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat and is the creator
        if (!chatService.isUserChatCreator(userDetails.getId(), id)) {
            return ResponseEntity.status(403).build();
        }
        
        chatService.deleteChat(id);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Chat deleted successfully");
        }});
    }

    @PostMapping("/{chatId}/members/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addMemberToChat(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), chatId)) {
            return ResponseEntity.status(403).build();
        }
        
        chatService.addMemberToChat(chatId, userId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Member added successfully");
        }});
    }

    @DeleteMapping("/{chatId}/members/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> removeMemberFromChat(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat and is either removing themselves or is the creator
        if (!(userDetails.getId().equals(userId) || chatService.isUserChatCreator(userDetails.getId(), chatId))) {
            return ResponseEntity.status(403).build();
        }
        
        chatService.removeMemberFromChat(chatId, userId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Member removed successfully");
        }});
    }

    @GetMapping("/{chatId}/members")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getChatMembers(
            @PathVariable Long chatId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Verify user has access to this chat
        if (!chatService.isUserInChat(userDetails.getId(), chatId)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(chatService.getChatMembers(chatId));
    }

    @GetMapping("/direct/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ChatDto> getOrCreateDirectChat(
            @PathVariable Long userId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ChatDto chatDto = chatService.getOrCreateDirectChat(userDetails.getId(), userId);
        return ResponseEntity.ok(chatDto);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatDto>> searchChats(
            @RequestParam String query,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ChatDto> chats = chatService.searchChats(query, userDetails.getId());
        return ResponseEntity.ok(chats);
    }
}