package com.example.EmailNotificationSystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTemplateRequest {
    private String name;
    private String subject;
    private String htmlBody;
    private List<String> variableKeys;
    private Boolean active;
}
