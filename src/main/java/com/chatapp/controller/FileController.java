package com.chatapp.controller;

import com.chatapp.model.Attachment;
import com.chatapp.model.dto.AttachmentDto;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FileController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Upload profile picture for the current user
     */
    @PostMapping("/users/me/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            Attachment attachment = fileStorageService.storeProfilePicture(file, userId);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/attachments/")
                    .path(attachment.getId().toString())
                    .toUriString();
            
            AttachmentDto attachmentDto = convertToDto(attachment, fileDownloadUri);
            
            return ResponseEntity.ok().body(attachmentDto);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload the file: " + ex.getMessage());
        }
    }
    
    /**
     * Upload attachment for a message
     */
    @PostMapping("/messages/{messageId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadMessageAttachment(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long messageId,
            Authentication authentication) {
        try {
            // Security check would be implemented here to verify user can attach to this message
            
            Attachment attachment = fileStorageService.storeMessageAttachment(file, messageId);
            
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/attachments/")
                    .path(attachment.getId().toString())
                    .toUriString();
            
            AttachmentDto attachmentDto = convertToDto(attachment, fileDownloadUri);
            
            return ResponseEntity.ok().body(attachmentDto);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload the file: " + ex.getMessage());
        }
    }
    
    /**
     * Upload multiple attachments for a message
     */
    @PostMapping("/messages/{messageId}/attachments/multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadMultipleMessageAttachments(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable Long messageId,
            Authentication authentication) {
        try {
            List<AttachmentDto> results = new ArrayList<>();
            
            for (MultipartFile file : files) {
                Attachment attachment = fileStorageService.storeMessageAttachment(file, messageId);
                
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/attachments/")
                        .path(attachment.getId().toString())
                        .toUriString();
                
                results.add(convertToDto(attachment, fileDownloadUri));
            }
            
            return ResponseEntity.ok().body(results);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload one or more files: " + ex.getMessage());
        }
    }
    
    /**
     * Download an attachment
     */
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        Attachment attachment = fileStorageService.getAttachment(attachmentId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + attachmentId));
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(new ByteArrayResource(attachment.getFileContent()));
    }
    
    /**
     * Get all attachments for the current user
     */
    @GetMapping("/users/me/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentDto>> getUserAttachments(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        List<Attachment> attachments = fileStorageService.getUserAttachments(userId);
        
        List<AttachmentDto> attachmentDtos = attachments.stream()
                .map(attachment -> {
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/attachments/")
                            .path(attachment.getId().toString())
                            .toUriString();
                    return convertToDto(attachment, fileDownloadUri);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(attachmentDtos);
    }
    
    /**
     * Get a user's profile picture
     */
    @GetMapping("/users/{userId}/profile-picture")
    public ResponseEntity<Resource> getUserProfilePicture(@PathVariable Long userId) {
        Attachment attachment = fileStorageService.getUserProfilePicture(userId)
                .orElseThrow(() -> new RuntimeException("Profile picture not found for user with id " + userId));
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .body(new ByteArrayResource(attachment.getFileContent()));
    }
    
    /**
     * Helper method to convert Attachment to DTO
     */
    private AttachmentDto convertToDto(Attachment attachment, String fileUrl) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileUrl(fileUrl)
                .fileSize(attachment.getFileSize())
                .type(attachment.getType().name())
                .thumbnailUrl(attachment.getThumbnailUrl())
                .createdAt(attachment.getCreatedAt().toString())
                .build();
    }
}