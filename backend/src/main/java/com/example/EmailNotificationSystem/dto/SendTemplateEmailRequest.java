package com.example.EmailNotificationSystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class SendTemplateEmailRequest {

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    @NotEmpty(message = "Atleast one recipient email is required")
    private List<@Email(message = "Invalid email format") @NotEmpty String> toEmails;

    private Map<String , String> variables;

}
