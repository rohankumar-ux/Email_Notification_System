package com.example.EmailNotificationSystem.converter;

import org.springframework.stereotype.Component;

import com.example.EmailNotificationSystem.dto.EmailDetailResponse;
import com.example.EmailNotificationSystem.model.Email;

@Component
public class EmailDetailResponseConverter {
    
    public EmailDetailResponse convertToResponse(Email email) {
        return EmailDetailResponse.builder()
                .fromEmail(email.getFromEmail())
                .toEmails(email.getToEmailList())
                .subject(email.getSubject())
                .body(email.getBody())
                .html(email.isHtml())
                .status(email.getStatus())
                .templateName(email.getEmailTemplate() != null ? email.getEmailTemplate().getName() : null)
                .templateVariables(email.getTemplateVariables())
                .sendgridMessageId(email.getSendgridMessageId())
                .errorMessage(email.getErrorMessage())
                .createdAt(email.getCreatedAt())
                .updatedAt(email.getUpdatedAt())
                .sentAt(email.getSentAt())
                .deliveredAt(email.getDeliveredAt())
                .build();
    }
}
