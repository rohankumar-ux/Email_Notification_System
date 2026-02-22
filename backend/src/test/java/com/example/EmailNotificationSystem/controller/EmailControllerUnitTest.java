package com.example.EmailNotificationSystem.controller;

import com.example.EmailNotificationSystem.dto.*;
import com.example.EmailNotificationSystem.enums.EmailStatus;
import com.example.EmailNotificationSystem.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailControllerUnitTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    private SendRawEmailRequest rawEmailRequest;
    private SendTemplateEmailRequest templateEmailRequest;
    private EmailResponse emailResponse;
    private EmailDetailResponse emailDetailResponse;
    private DashboardStatsResponse statsResponse;

    @BeforeEach
    void setUp() {
        rawEmailRequest = new SendRawEmailRequest();
        rawEmailRequest.setToEmails(List.of("recipient@example.com"));
        rawEmailRequest.setSubject("Test Subject");
        rawEmailRequest.setBody("Test Body");
        rawEmailRequest.setHtml(false);

        templateEmailRequest = new SendTemplateEmailRequest();
        templateEmailRequest.setTemplateId(UUID.randomUUID());
        templateEmailRequest.setToEmails(List.of("recipient@example.com"));
        templateEmailRequest.setVariables(Map.of("name", "John"));

        emailResponse = EmailResponse.builder()
            .id(UUID.randomUUID())
            .fromEmail("sender@example.com")
            .toEmails(List.of("recipient@example.com"))
            .subject("Test Subject")
            .status(EmailStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        emailDetailResponse = EmailDetailResponse.builder()
            .fromEmail("sender@example.com")
            .toEmails(List.of("recipient@example.com"))
            .subject("Test Subject")
            .body("Test Body")
            .html(false)
            .status(EmailStatus.SENT)
            .createdAt(LocalDateTime.now())
            .build();

        statsResponse = DashboardStatsResponse.builder()
            .totalEmails(100L)
            .pending(10L)
            .queued(20L)
            .sent(50L)
            .delivered(40L)
            .failed(15L)
            .bounced(5L)
            .build();
    }

    @Test
    void sendRawEmail_Success() {
        when(emailService.sendRawEmail(any(SendRawEmailRequest.class))).thenReturn(emailResponse);

        ResponseEntity<ApiResponse<EmailResponse>> result = emailController.sendRawEmail(rawEmailRequest);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Email queued for delivery", result.getBody().getMessage());
        assertEquals(emailResponse, result.getBody().getData());
    }

    @Test
    void sendTemplateEmail_Success() {
        when(emailService.sendTemplateEmail(any(SendTemplateEmailRequest.class))).thenReturn(emailResponse);

        ResponseEntity<ApiResponse<EmailResponse>> result = emailController.sendTemplateEmail(templateEmailRequest);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Email queued for delivery", result.getBody().getMessage());
        assertEquals(emailResponse, result.getBody().getData());
    }

    @Test
    void sendTestEmail_Success() {
        UUID emailId = UUID.randomUUID();
        when(emailService.sendTestEmail(emailId)).thenReturn(emailResponse);

        ResponseEntity<ApiResponse<EmailResponse>> result = emailController.sendTestEmail(emailId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Test email sent", result.getBody().getMessage());
        assertEquals(emailResponse, result.getBody().getData());
    }

    @Test
    void getEmail_Success() {
        UUID emailId = UUID.randomUUID();
        when(emailService.getEmailById(emailId)).thenReturn(emailDetailResponse);

        ResponseEntity<ApiResponse<EmailDetailResponse>> result = emailController.getEmail(emailId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Email retrieved", result.getBody().getMessage());
        assertEquals(emailDetailResponse, result.getBody().getData());
    }

    @Test
    void getEmails_WithFilters_Success() {
        PagedResponse<EmailResponse> pagedResponse = PagedResponse.<EmailResponse>builder()
            .content(List.of(emailResponse))
            .page(0)
            .size(20)
            .totalElements(1L)
            .totalPages(1)
            .last(true)
            .build();

        when(emailService.getEmails(any(), any(), any(), any())).thenReturn(pagedResponse);

        ResponseEntity<ApiResponse<PagedResponse<EmailResponse>>> result = emailController.getEmails(
            EmailStatus.SENT, LocalDateTime.now().minusDays(1), LocalDateTime.now(), 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(pagedResponse, result.getBody().getData());
    }

    @Test
    void getEmails_NoFilters_Success() {
        PagedResponse<EmailResponse> pagedResponse = PagedResponse.<EmailResponse>builder()
            .content(List.of())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .last(true)
            .build();

        when(emailService.getEmails(eq(null), eq(null), eq(null), any())).thenReturn(pagedResponse);

        ResponseEntity<ApiResponse<PagedResponse<EmailResponse>>> result = emailController.getEmails(
            null, null, null, 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(pagedResponse, result.getBody().getData());
    }

    @Test
    void getStats_Success() {
        when(emailService.getDashboardStats()).thenReturn(statsResponse);

        ResponseEntity<ApiResponse<DashboardStatsResponse>> result = emailController.getStats();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Stats retrieved", result.getBody().getMessage());
        assertEquals(statsResponse, result.getBody().getData());
    }
}
