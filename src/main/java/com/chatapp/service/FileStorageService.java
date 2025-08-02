package com.chatapp.service;

import com.chatapp.config.FileUploadSecurityConfig.FileUploadValidator;
import com.chatapp.model.Attachment;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.repository.AttachmentRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileStorageService {
    
    @Autowired
    private AttachmentRepository attachmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private FileUploadValidator fileUploadValidator;

    /**
     * Store a profile picture for a user
     */
    public Attachment storeProfilePicture(MultipartFile file, Long userId) throws IOException {
        // Validate the file
        fileUploadValidator.validateProfilePicture(file);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check for existing profile picture and delete if exists
        attachmentRepository.findByUserIdAndIsProfilePicture(userId, true)
                .forEach(attachmentRepository::delete);
        
        Attachment attachment = Attachment.builder()
                .fileName(sanitizeFileName(file.getOriginalFilename()))
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileContent(file.getBytes())
                .isProfilePicture(true)
                .type(determineAttachmentType(file.getContentType()))
                .user(user)
                .build();
        
        // Save the attachment
        attachment = attachmentRepository.save(attachment);
        
        // Update user's profile picture reference
        user.setProfilePicture("/api/attachments/" + attachment.getId());
        userRepository.save(user);
        
        return attachment;
    }
    
    /**
     * Store a file attachment for a message
     */
    public Attachment storeMessageAttachment(MultipartFile file, Long messageId) throws IOException {
        // Validate the file
        fileUploadValidator.validateAttachment(file);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));
        
        Attachment attachment = Attachment.builder()
                .fileName(sanitizeFileName(file.getOriginalFilename()))
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileContent(file.getBytes())
                .type(determineAttachmentType(file.getContentType()))
                .build();
        
        attachment = attachmentRepository.save(attachment);
        
        // Add attachment to message
        message.getAttachments().add(attachment);
        messageRepository.save(message);
        
        return attachment;
    }
    
    /**
     * Get attachment by ID
     */
    public Optional<Attachment> getAttachment(Long id) {
        return attachmentRepository.findById(id);
    }
    
    /**
     * Get all attachments for a user
     */
    public List<Attachment> getUserAttachments(Long userId) {
        return attachmentRepository.findByUserId(userId);
    }
    
    /**
     * Get user's profile picture
     */
    public Optional<Attachment> getUserProfilePicture(Long userId) {
        return attachmentRepository.findByUserIdAndIsProfilePicture(userId, true)
                .stream().findFirst();
    }
    
    /**
     * Determine attachment type based on content type
     */
    private Attachment.AttachmentType determineAttachmentType(String contentType) {
        if (contentType == null) {
            return Attachment.AttachmentType.OTHER;
        }
        
        if (contentType.startsWith("image/")) {
            return Attachment.AttachmentType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return Attachment.AttachmentType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return Attachment.AttachmentType.AUDIO;
        } else if (contentType.equals("application/pdf") || 
                  contentType.contains("document") || 
                  contentType.contains("text/")) {
            return Attachment.AttachmentType.DOCUMENT;
        } else {
            return Attachment.AttachmentType.OTHER;
        }
    }
    
    /**
     * Sanitize file name to prevent path traversal and injection attacks
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed_file";
        }
        
        // Replace any directory traversal characters
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        // Limit filename length
        if (sanitized.length() > 100) {
            int lastDotIndex = sanitized.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String extension = sanitized.substring(lastDotIndex);
                sanitized = sanitized.substring(0, 96) + extension;
            } else {
                sanitized = sanitized.substring(0, 100);
            }
        }
        
        return sanitized;
    }
}