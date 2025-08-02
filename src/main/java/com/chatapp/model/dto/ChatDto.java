package com.chatapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatDto {
    private Long id;
    
    @NotBlank
    private String name;
    
    private String type;
    private String avatarUrl;
    private String description;
    private UserDto creator;
    private List<UserDto> members;
    private Integer unreadCount;
    private MessageDto lastMessage;
    private String createdAt;
    private String updatedAt;
}