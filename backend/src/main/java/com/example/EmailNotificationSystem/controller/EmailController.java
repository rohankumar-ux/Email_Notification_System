package com.example.EmailNotificationSystem.controller;

import com.example.EmailNotificationSystem.dto.SendTemplateEmailRequest;
import com.example.EmailNotificationSystem.enums.EmailStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EmailNotificationSystem.dto.EmailDetailResponse;
import com.example.EmailNotificationSystem.dto.ApiResponse;
import com.example.EmailNotificationSystem.dto.DashboardStatsResponse;
import com.example.EmailNotificationSystem.dto.EmailResponse;
import com.example.EmailNotificationSystem.dto.PagedResponse;
import com.example.EmailNotificationSystem.dto.SendRawEmailRequest;
import com.example.EmailNotificationSystem.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmailResponse>> sendRawEmail(
            @Valid @RequestBody SendRawEmailRequest request) {

        EmailResponse response = emailService.sendRawEmail(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Email queued for delivery", response));
    }

    @PostMapping("/send-template")
    public ResponseEntity<ApiResponse<EmailResponse>> sendTemplateEmail(
            @Valid @RequestBody SendTemplateEmailRequest request) {
        EmailResponse response = emailService.sendTemplateEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Email queued for delivery", response));
    }

    @PostMapping("/{id}/send-test")
    public ResponseEntity<ApiResponse<EmailResponse>> sendTestEmail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Test email sent", emailService.sendTestEmail(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmailDetailResponse>> getEmail(@PathVariable UUID id) {
        EmailDetailResponse response = emailService.getEmailById(id);
        return ResponseEntity.ok(ApiResponse.ok("Email retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EmailResponse>>> getEmails(
            @RequestParam(required = false) EmailStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<EmailResponse> result = emailService.getEmails(status, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = emailService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Stats retrieved", stats));
    }

}
