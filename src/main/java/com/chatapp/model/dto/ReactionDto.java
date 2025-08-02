package com.chatapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionDto {
    private Long id;
    private String emoji;
    private String name;
    private UserDto user;
    private String createdAt;
}