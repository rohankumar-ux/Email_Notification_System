package com.example.EmailNotificationSystem.converter;

import com.example.EmailNotificationSystem.dto.EmailResponse;
import com.example.EmailNotificationSystem.model.Email;

import org.springframework.stereotype.Component;

@Component
public class EmailResponseConverter {

    public EmailResponse convertToResponse(Email email) {
        return EmailResponse.builder()
                .id(email.getId())
                .fromEmail(email.getFromEmail())
                .toEmails(email.getToEmailList())
                .subject(email.getSubject())
                .status(email.getStatus())
                .templateName(email.getEmailTemplate() != null ? email.getEmailTemplate().getName() : null)
                .errorMessage(email.getErrorMessage())
                .createdAt(email.getCreatedAt())
                .sentAt(email.getSentAt())
                .deliveredAt(email.getDeliveredAt())
                .build();
    }
}
