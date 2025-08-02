package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.model.dto.AttachmentDto;
import com.chatapp.model.dto.MessageDto;
import com.chatapp.model.dto.MessageRequest;
import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserService userService;

    private final Path fileStorageLocation = Paths.get("uploads/attachments").toAbsolutePath().normalize();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageService() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Page<MessageDto> findByChatId(Long chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        
        return messages.map(this::convertToDto);
    }

    public MessageDto findById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        
        return convertToDto(message);
    }

    @Transactional
    public Message createMessage(MessageDto messageDto, User sender, Chat chat) {
        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setSender(sender);
        message.setChat(chat);
        
        if (messageDto.getReplyToId() != null) {
            messageRepository.findById(messageDto.getReplyToId())
                    .ifPresent(message::setReplyTo);
        }
        
        return messageRepository.save(message);
    }

    @Transactional
    public MessageDto createMessage(MessageRequest messageRequest, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + senderId));
        
        Chat chat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id: " + messageRequest.getChatId()));
        
        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setSender(sender);
        message.setChat(chat);
        
        if (messageRequest.getReplyToId() != null) {
            messageRepository.findById(messageRequest.getReplyToId())
                    .ifPresent(message::setReplyTo);
        }
        
        Message savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }

    @Transactional
    public String addAttachment(Long messageId, MultipartFile file) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = fileStorageLocation.resolve(fileName);
        
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFilePath("/uploads/attachments/" + fileName);
            attachment.setFileSize(file.getSize());
            
            // Determine type from content type
            if (file.getContentType() != null) {
                if (file.getContentType().startsWith("image/")) {
                    attachment.setType(Attachment.AttachmentType.IMAGE);
                } else if (file.getContentType().startsWith("video/")) {
                    attachment.setType(Attachment.AttachmentType.VIDEO);
                } else if (file.getContentType().startsWith("audio/")) {
                    attachment.setType(Attachment.AttachmentType.AUDIO);
                } else {
                    attachment.setType(Attachment.AttachmentType.DOCUMENT);
                }
            } else {
                attachment.setType(Attachment.AttachmentType.OTHER);
            }
            
            Attachment savedAttachment = attachmentRepository.save(attachment);
            
            if (message.getAttachments() == null) {
                message.setAttachments(new HashSet<>());
            }
            
            message.getAttachments().add(savedAttachment);
            messageRepository.save(message);
            
            return attachment.getFilePath();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    @Transactional
    public MessageDto updateMessage(Long id, Map<String, Object> updates) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + id));
        
        if (updates.containsKey("content")) {
            message.setContent((String) updates.get("content"));
            message.setEdited(true);
            message.setEditedAt(LocalDateTime.now());
        }
        
        Message updatedMessage = messageRepository.save(message);
        return convertToDto(updatedMessage);
    }

    @Transactional
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        if (message.getReadBy() == null) {
            message.setReadBy(new HashSet<>());
        }
        
        message.getReadBy().add(user);
        messageRepository.save(message);
    }

    @Transactional
    public void addReaction(Long messageId, Long userId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Remove any existing reaction with the same emoji from this user
        List<Reaction> existingReactions = reactionRepository.findByMessageIdAndUserId(messageId, userId);
        existingReactions.stream()
                .filter(r -> r.getEmoji().equals(emoji))
                .forEach(reactionRepository::delete);
        
        Reaction reaction = new Reaction();
        reaction.setEmoji(emoji);
        reaction.setName(emoji); // Simple name based on emoji
        reaction.setUser(user);
        
        Reaction savedReaction = reactionRepository.save(reaction);
        
        if (message.getReactions() == null) {
            message.setReactions(new HashSet<>());
        }
        
        message.getReactions().add(savedReaction);
        messageRepository.save(message);
    }

    @Transactional
    public void removeReaction(Long messageId, Long userId, String emoji) {
        List<Reaction> reactions = reactionRepository.findByMessageIdAndUserId(messageId, userId);
        
        reactions.stream()
                .filter(r -> r.getEmoji().equals(emoji))
                .forEach(reactionRepository::delete);
    }

    public List<MessageDto> searchMessagesInChat(String query, Long chatId) {
        List<Message> messages = messageRepository.searchMessagesInChat(chatId, query);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> searchMessagesForUser(String query, Long userId) {
        List<Message> messages = messageRepository.searchMessagesForUser(query, userId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MessageDto convertToDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setChatId(message.getChat().getId());
        
        if (message.getSender() != null) {
            dto.setSender(userService.convertToDto(message.getSender()));
        }
        
        if (message.getReplyTo() != null) {
            dto.setReplyToId(message.getReplyTo().getId());
            // Simple reply without nesting too deep
            MessageDto replyDto = new MessageDto();
            replyDto.setId(message.getReplyTo().getId());
            replyDto.setContent(message.getReplyTo().getContent());
            
            if (message.getReplyTo().getSender() != null) {
                replyDto.setSender(userService.convertToDto(message.getReplyTo().getSender()));
            }
            
            dto.setReplyTo(replyDto);
        }
        
        if (message.getAttachments() != null) {
            List<AttachmentDto> attachmentDtos = message.getAttachments().stream()
                    .map(attachment -> {
                        AttachmentDto attachmentDto = new AttachmentDto();
                        attachmentDto.setId(attachment.getId());
                        attachmentDto.setFileName(attachment.getFileName());
                        attachmentDto.setFileType(attachment.getFileType());
                        attachmentDto.setFileUrl(attachment.getFilePath());
                        attachmentDto.setFileSize(attachment.getFileSize());
                        attachmentDto.setType(attachment.getType().name());
                        attachmentDto.setThumbnailUrl(attachment.getThumbnailUrl());
                        
                        if (attachment.getCreatedAt() != null) {
                            attachmentDto.setCreatedAt(attachment.getCreatedAt().format(formatter));
                        }
                        
                        return attachmentDto;
                    })
                    .collect(Collectors.toList());
            
            dto.setAttachments(attachmentDtos);
        }
        
        if (message.getReactions() != null) {
            // Group reactions by emoji
            Map<String, Integer> reactionCounts = message.getReactions().stream()
                    .collect(Collectors.groupingBy(Reaction::getEmoji, Collectors.summingInt(r -> 1)));
            
            dto.setReactions(reactionCounts);
        }
        
        dto.setEdited(message.isEdited());
        
        if (message.getEditedAt() != null) {
            dto.setEditedAt(message.getEditedAt().format(formatter));
        }
        
        dto.setDeleted(message.isDeleted());
        
        if (message.getReadBy() != null) {
            List<UserDto> readByUsers = message.getReadBy().stream()
                    .map(userService::convertToDto)
                    .collect(Collectors.toList());
            
            dto.setReadBy(readByUsers);
        }
        
        if (message.getCreatedAt() != null) {
            dto.setCreatedAt(message.getCreatedAt().format(formatter));
        }
        
        return dto;
    }
}