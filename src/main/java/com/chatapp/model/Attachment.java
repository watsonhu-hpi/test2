package com.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    
    // Store file content directly in the database
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] fileContent;
    
    @Enumerated(EnumType.STRING)
    private AttachmentType type;
    
    // Store thumbnail data directly in the database
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] thumbnailContent;
    
    private String thumbnailUrl;
    
    // Reference to user (for profile pictures)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Reference to message (for shared files)
    @ManyToMany(mappedBy = "attachments")
    private Set<Message> messages = new HashSet<>();
    
    // Whether the attachment is a profile picture
    private boolean isProfilePicture = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AttachmentType {
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        OTHER
    }
}