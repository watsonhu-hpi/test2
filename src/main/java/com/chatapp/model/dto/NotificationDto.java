package com.chatapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String content;
    private UserDto relatedUser;
    private ChatDto relatedChat;
    private MessageDto relatedMessage;
    private boolean read;
    private String readAt;
    private String createdAt;
}