package com.chatapp.service;

import com.chatapp.model.Chat;
import com.chatapp.model.User;
import com.chatapp.model.dto.ChatDto;
import com.chatapp.model.dto.MessageDto;
import com.chatapp.model.dto.UserDto;
import com.chatapp.repository.ChatRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ChatDto> findChatsByUserId(Long userId) {
        List<Chat> chats = chatRepository.findByMemberId(userId);
        return chats.stream()
                .map(chat -> convertToDto(chat, userId))
                .collect(Collectors.toList());
    }

    public Chat findById(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id: " + id));
    }

    public ChatDto findDtoById(Long id) {
        return convertToDto(findById(id), null);
    }

    public ChatDto findDtoById(Long id, Long userId) {
        return convertToDto(findById(id), userId);
    }

    @Transactional
    public ChatDto createChat(Map<String, Object> chatRequest, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + creatorId));
        
        Chat chat = new Chat();
        chat.setName((String) chatRequest.get("name"));
        chat.setDescription((String) chatRequest.getOrDefault("description", ""));
        chat.setType(Chat.ChatType.valueOf((String) chatRequest.getOrDefault("type", "GROUP")));
        chat.setAvatarUrl((String) chatRequest.getOrDefault("avatarUrl", ""));
        chat.setCreator(creator);
        
        Set<User> members = new HashSet<>();
        members.add(creator);
        
        // Add other members if provided
        if (chatRequest.containsKey("memberIds")) {
            List<Long> memberIds = (List<Long>) chatRequest.get("memberIds");
            for (Long memberId : memberIds) {
                userRepository.findById(memberId).ifPresent(members::add);
            }
        }
        
        chat.setMembers(members);
        Chat savedChat = chatRepository.save(chat);
        
        return convertToDto(savedChat, creatorId);
    }

    @Transactional
    public ChatDto updateChat(Long chatId, Map<String, Object> updates) {
        Chat chat = findById(chatId);
        
        if (updates.containsKey("name")) {
            chat.setName((String) updates.get("name"));
        }
        
        if (updates.containsKey("description")) {
            chat.setDescription((String) updates.get("description"));
        }
        
        if (updates.containsKey("avatarUrl")) {
            chat.setAvatarUrl((String) updates.get("avatarUrl"));
        }
        
        return convertToDto(chatRepository.save(chat), null);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    @Transactional
    public void addMemberToChat(Long chatId, Long userId) {
        Chat chat = findById(chatId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        chat.getMembers().add(user);
        chatRepository.save(chat);
    }

    @Transactional
    public void removeMemberFromChat(Long chatId, Long userId) {
        Chat chat = findById(chatId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        chat.getMembers().remove(user);
        chatRepository.save(chat);
    }

    public List<UserDto> getChatMembers(Long chatId) {
        Chat chat = findById(chatId);
        
        return chat.getMembers().stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatDto getOrCreateDirectChat(Long user1Id, Long user2Id) {
        // Try to find existing direct chat between these users
        Optional<Chat> existingChat = chatRepository.findDirectChatBetweenUsers(user1Id, user2Id);
        
        if (existingChat.isPresent()) {
            return convertToDto(existingChat.get(), user1Id);
        }
        
        // Create a new direct chat
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user1Id));
        
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user2Id));
        
        Chat chat = new Chat();
        chat.setName(user2.getUsername()); // From user1's perspective
        chat.setType(Chat.ChatType.DIRECT);
        chat.setCreator(user1);
        
        Set<User> members = new HashSet<>();
        members.add(user1);
        members.add(user2);
        chat.setMembers(members);
        
        Chat savedChat = chatRepository.save(chat);
        return convertToDto(savedChat, user1Id);
    }

    public List<ChatDto> searchChats(String query, Long userId) {
        List<Chat> chats = chatRepository.searchChatsByNameForUser(query, userId);
        
        return chats.stream()
                .map(chat -> convertToDto(chat, userId))
                .collect(Collectors.toList());
    }

    public boolean isUserInChat(Long userId, Long chatId) {
        Chat chat = findById(chatId);
        return chat.getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    public boolean isUserChatCreator(Long userId, Long chatId) {
        Chat chat = findById(chatId);
        return chat.getCreator().getId().equals(userId);
    }

    public ChatDto convertToDto(Chat chat, Long currentUserId) {
        ChatDto dto = new ChatDto();
        dto.setId(chat.getId());
        dto.setName(chat.getName());
        dto.setType(chat.getType().name());
        dto.setAvatarUrl(chat.getAvatarUrl());
        dto.setDescription(chat.getDescription());
        
        if (chat.getCreator() != null) {
            dto.setCreator(userService.convertToDto(chat.getCreator()));
        }
        
        if (chat.getMembers() != null) {
            dto.setMembers(chat.getMembers().stream()
                    .map(userService::convertToDto)
                    .collect(Collectors.toList()));
        }
        
        if (chat.getCreatedAt() != null) {
            dto.setCreatedAt(chat.getCreatedAt().format(formatter));
        }
        
        if (chat.getUpdatedAt() != null) {
            dto.setUpdatedAt(chat.getUpdatedAt().format(formatter));
        }
        
        // Get last message in chat
        List<MessageDto> lastMessages = messageService.findByChatId(chat.getId(), 0, 1).getContent();
        if (!lastMessages.isEmpty()) {
            dto.setLastMessage(lastMessages.get(0));
        }
        
        // Count unread messages for current user
        if (currentUserId != null) {
            dto.setUnreadCount(messageRepository.countUnreadMessagesForUser(chat.getId(), currentUserId).intValue());
        }
        
        return dto;
    }
}