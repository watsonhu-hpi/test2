package com.chatapp.websocket;

import com.chatapp.model.Chat;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.model.dto.MessageDto;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.ChatService;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ChatService chatService;
    
    // Store online users and their sessions
    private final Map<Long, String> userSessions = new HashMap<>();

    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable Long chatId,
                            @Payload MessageDto messageDto,
                            SimpMessageHeaderAccessor headerAccessor,
                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        User user = userService.findById(userId);
        Chat chat = chatService.findById(chatId);
        
        // Check if user is part of chat
        if (!chatService.isUserInChat(userId, chatId)) {
            logger.error("User {} is not part of chat {}", userId, chatId);
            return;
        }
        
        // Create and save message
        Message savedMessage = messageService.createMessage(messageDto, user, chat);
        MessageDto savedMessageDto = messageService.convertToDto(savedMessage);
        
        // Send to chat topic
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessageDto);
        
        // Send to offline users' queues for retrieval when they come online
        chat.getMembers().forEach(member -> {
            if (!userSessions.containsKey(member.getId()) && !member.getId().equals(userId)) {
                messagingTemplate.convertAndSendToUser(
                        member.getUsername(),
                        "/queue/messages",
                        savedMessageDto
                );
            }
        });
    }

    @MessageMapping("/chat/{chatId}/typing")
    public void sendTypingIndicator(@DestinationVariable Long chatId,
                                    Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Map<String, Object> typingEvent = new HashMap<>();
        typingEvent.put("userId", userDetails.getId());
        typingEvent.put("username", userDetails.getUsername());
        typingEvent.put("typing", true);
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", typingEvent);
    }

    @MessageMapping("/chat/{chatId}/read")
    public void markAsRead(@DestinationVariable Long chatId,
                           @Payload Map<String, Object> payload,
                           Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        Long messageId = Long.valueOf(payload.get("messageId").toString());
        
        // Mark message as read by user
        messageService.markAsRead(messageId, userId);
        
        // Notify others in chat about read status
        Map<String, Object> readEvent = new HashMap<>();
        readEvent.put("messageId", messageId);
        readEvent.put("userId", userId);
        readEvent.put("username", userDetails.getUsername());
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/read", readEvent);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Get connected user
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            String sessionId = headerAccessor.getSessionId();
            
            // Store user's session
            userSessions.put(userId, sessionId);
            
            // Update user's online status
            userService.updateStatus(userId, "online");
            
            // Broadcast user's online status
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("userId", userId);
            statusUpdate.put("status", "online");
            messagingTemplate.convertAndSend("/topic/users/status", statusUpdate);
            
            logger.info("User connected: {} ({})", userDetails.getUsername(), sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Find disconnected user
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Remove user's session
            userSessions.remove(userId);
            
            // Update user's online status
            userService.updateLastActive(userId);
            userService.updateStatus(userId, "offline");
            
            // Broadcast user's offline status
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("userId", userId);
            statusUpdate.put("status", "offline");
            messagingTemplate.convertAndSend("/topic/users/status", statusUpdate);
            
            logger.info("User disconnected: {}", userDetails.getUsername());
        }
    }
}