package com.example.EmailNotificationSystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SendRawEmailRequest {

    @NotEmpty(message = "Atleast one recipient email is required")
    private List<@Email(message = "Invalid email format") @NotEmpty String> toEmails;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private boolean html = false;
}
