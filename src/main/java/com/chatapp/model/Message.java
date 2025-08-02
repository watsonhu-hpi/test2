package com.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @OneToMany(mappedBy = "replyTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> replies = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "message_attachments",
               joinColumns = @JoinColumn(name = "message_id"),
               inverseJoinColumns = @JoinColumn(name = "attachment_id"))
    private Set<Attachment> attachments = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "message_reactions",
               joinColumns = @JoinColumn(name = "message_id"),
               inverseJoinColumns = @JoinColumn(name = "reaction_id"))
    private Set<Reaction> reactions = new HashSet<>();

    private boolean edited;
    private LocalDateTime editedAt;
    private boolean deleted;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "message_read_by",
               joinColumns = @JoinColumn(name = "message_id"),
               inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> readBy = new HashSet<>();

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
}