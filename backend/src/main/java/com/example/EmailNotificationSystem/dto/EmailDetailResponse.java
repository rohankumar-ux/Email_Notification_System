package com.example.EmailNotificationSystem.dto;

import com.example.EmailNotificationSystem.enums.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmailDetailResponse {
    private String fromEmail;
    private List<String> toEmails;
    private String subject;
    private String body;
    private boolean html;
    private EmailStatus status;
    private String templateName;
    private String templateVariables;
    private String sendgridMessageId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
}
