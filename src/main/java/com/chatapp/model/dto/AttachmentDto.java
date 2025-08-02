package com.chatapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private String type;
    private String thumbnailUrl;
    private String createdAt;
}