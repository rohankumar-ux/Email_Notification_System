package com.example.EmailNotificationSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TemplateResponse {
    private UUID id;
    private String name;
    private String subject;
    private String htmlBody;
    private List<String> variableKeys;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
