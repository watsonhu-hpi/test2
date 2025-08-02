package com.chatapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    
    @NotBlank
    private String content;
    
    @NotNull
    private Long chatId;
    
    private UserDto sender;
    private Long replyToId;
    private MessageDto replyTo;
    private List<AttachmentDto> attachments;
    private Map<String, Integer> reactions;
    private boolean edited;
    private String editedAt;
    private boolean deleted;
    private List<UserDto> readBy;
    private String createdAt;
}