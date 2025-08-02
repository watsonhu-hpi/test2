package com.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class FileUploadSecurityConfig {

    // Maximum size for profile pictures (default 5MB)
    @Value("${chatapp.upload.profile-picture.max-size:5242880}")
    private long maxProfilePictureSize;
    
    // Maximum size for attachments (default 10MB)
    @Value("${chatapp.upload.attachment.max-size:10485760}")
    private long maxAttachmentSize;
    
    // Allowed mime types for profile pictures
    private static final Set<String> ALLOWED_PROFILE_PICTURE_TYPES = new HashSet<>(
        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/svg+xml")
    );
    
    // Allowed mime types for attachments
    private static final Set<String> ALLOWED_ATTACHMENT_TYPES = new HashSet<>(
        Arrays.asList(
            // Images
            "image/jpeg", "image/png", "image/gif", "image/svg+xml", 
            // Documents
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            // Audio
            "audio/mpeg", "audio/wav", "audio/ogg",
            // Video
            "video/mp4", "video/mpeg", "video/webm"
        )
    );
    
    // Disallowed file extensions
    private static final Set<String> DISALLOWED_EXTENSIONS = new HashSet<>(
        Arrays.asList(
            "exe", "bat", "cmd", "sh", "php", "pl", "py", "js", "jsp", "htm", "html"
        )
    );

    @Bean
    public FileUploadValidator fileUploadValidator() {
        return new FileUploadValidator(
            ALLOWED_PROFILE_PICTURE_TYPES,
            ALLOWED_ATTACHMENT_TYPES,
            DISALLOWED_EXTENSIONS,
            maxProfilePictureSize,
            maxAttachmentSize
        );
    }
    
    /**
     * Validator class for file uploads
     */
    public static class FileUploadValidator {
        private final Set<String> allowedProfilePictureTypes;
        private final Set<String> allowedAttachmentTypes;
        private final Set<String> disallowedExtensions;
        private final long maxProfilePictureSize;
        private final long maxAttachmentSize;
        
        public FileUploadValidator(
                Set<String> allowedProfilePictureTypes,
                Set<String> allowedAttachmentTypes,
                Set<String> disallowedExtensions,
                long maxProfilePictureSize,
                long maxAttachmentSize) {
            this.allowedProfilePictureTypes = allowedProfilePictureTypes;
            this.allowedAttachmentTypes = allowedAttachmentTypes;
            this.disallowedExtensions = disallowedExtensions;
            this.maxProfilePictureSize = maxProfilePictureSize;
            this.maxAttachmentSize = maxAttachmentSize;
        }
        
        /**
         * Validate a profile picture upload
         */
        public void validateProfilePicture(MultipartFile file) {
            validateFileType(file, allowedProfilePictureTypes, "Profile picture must be a valid image file");
            validateFileSize(file, maxProfilePictureSize, "Profile picture exceeds maximum allowed size");
        }
        
        /**
         * Validate an attachment upload
         */
        public void validateAttachment(MultipartFile file) {
            validateFileType(file, allowedAttachmentTypes, "Attachment type not allowed");
            validateFileSize(file, maxAttachmentSize, "Attachment exceeds maximum allowed size");
            validateFileExtension(file);
        }
        
        private void validateFileType(MultipartFile file, Set<String> allowedTypes, String errorMessage) {
            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
        
        private void validateFileSize(MultipartFile file, long maxSize, String errorMessage) {
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
        
        private void validateFileExtension(MultipartFile file) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                int lastDotIndex = originalFilename.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
                    if (disallowedExtensions.contains(extension)) {
                        throw new IllegalArgumentException("File extension not allowed");
                    }
                }
            }
        }
    }
}