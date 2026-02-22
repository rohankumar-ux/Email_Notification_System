package com.example.EmailNotificationSystem.dto;

import com.example.EmailNotificationSystem.enums.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmailResponse {
    private UUID id;
    private String fromEmail;
    private List<String> toEmails;
    private String subject;
    private EmailStatus status;
    private String templateName;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
}
