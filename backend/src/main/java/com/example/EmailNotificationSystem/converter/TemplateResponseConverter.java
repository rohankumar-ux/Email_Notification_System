package com.example.EmailNotificationSystem.converter;

import org.springframework.stereotype.Component;

import com.example.EmailNotificationSystem.dto.TemplateResponse;
import com.example.EmailNotificationSystem.model.EmailTemplate;

@Component
public class TemplateResponseConverter {
    
    public TemplateResponse convertToResponse(EmailTemplate emailTemplate) {
        return TemplateResponse.builder()
                .id(emailTemplate.getId())
                .name(emailTemplate.getName())
                .subject(emailTemplate.getSubject())
                .htmlBody(emailTemplate.getHtmlBody())
                .variableKeys(emailTemplate.getVariableKeyList())
                .active(emailTemplate.isActive())
                .createdAt(emailTemplate.getCreatedAt())
                .updatedAt(emailTemplate.getUpdatedAt())
                .build();
    }
}
