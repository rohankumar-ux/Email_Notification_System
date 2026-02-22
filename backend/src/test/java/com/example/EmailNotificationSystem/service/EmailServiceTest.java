package com.example.EmailNotificationSystem.service;

import com.example.EmailNotificationSystem.converter.*;
import com.example.EmailNotificationSystem.dto.*;
import com.example.EmailNotificationSystem.enums.EmailStatus;
import com.example.EmailNotificationSystem.exception.ResourceNotFoundException;
import com.example.EmailNotificationSystem.messaging.EmailProducer;
import com.example.EmailNotificationSystem.model.Email;
import com.example.EmailNotificationSystem.model.EmailTemplate;
import com.example.EmailNotificationSystem.repository.EmailRepository;
import com.example.EmailNotificationSystem.repository.EmailTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailRepository emailRepository;
    
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    
    @Mock
    private EmailProducer emailProducer;
    
    @Mock
    private EmailResponseConverter emailResponseConverter;
    
    @Mock
    private EmailDetailResponseConverter emailDetailResponseConverter;
    
    @Mock
    private PagedResponseConverter pagedResponseConverter;
    
    @Mock
    private SendGridService sendGridService;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailService emailService;

    private Email testEmail;
    private EmailTemplate testTemplate;
    private SendRawEmailRequest rawEmailRequest;
    private SendTemplateEmailRequest templateEmailRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "defaultFromEmail", "test@example.com");
        
        testEmail = Email.builder()
            .id(UUID.randomUUID())
            .fromEmail("test@example.com")
            .toEmails("recipient@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .html(false)
            .status(EmailStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        testTemplate = EmailTemplate.builder()
            .id(UUID.randomUUID())
            .name("Test Template")
            .subject("Template Subject")
            .htmlBody("<html>Template Body</html>")
            .variableKeys("name,email")
            .active(true)
            .build();

        rawEmailRequest = new SendRawEmailRequest();
        rawEmailRequest.setToEmails(List.of("recipient@example.com"));
        rawEmailRequest.setSubject("Test Subject");
        rawEmailRequest.setBody("Test Body");
        rawEmailRequest.setHtml(false);

        templateEmailRequest = new SendTemplateEmailRequest();
        templateEmailRequest.setTemplateId(testTemplate.getId());
        templateEmailRequest.setToEmails(List.of("recipient@example.com"));
        templateEmailRequest.setVariables(Map.of("name", "John", "email", "john@example.com"));
    }

    @Test
    void sendRawEmail_Success() {
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);
        when(emailResponseConverter.convertToResponse(testEmail)).thenReturn(EmailResponse.builder().build());
        doNothing().when(emailProducer).sendEmail(any(UUID.class));

        EmailResponse result = emailService.sendRawEmail(rawEmailRequest);

        assertNotNull(result);
        verify(emailRepository).save(any(Email.class));
        verify(emailProducer).sendEmail(testEmail.getId());
        verify(emailResponseConverter).convertToResponse(testEmail);
    }

    @Test
    void sendRawEmail_QueueFailure_HandlesException() {
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);
        doThrow(new RuntimeException("Queue error")).when(emailProducer).sendEmail(any(UUID.class));
        when(emailRepository.save(testEmail)).thenReturn(testEmail);

        assertThrows(RuntimeException.class, () -> emailService.sendRawEmail(rawEmailRequest));
        
        verify(emailRepository, times(2)).save(any(Email.class));
        assertEquals(EmailStatus.FAILED, testEmail.getStatus());
        assertTrue(testEmail.getErrorMessage().contains("Queue publish failed"));
    }

    @Test
    void sendTemplateEmail_TemplateNotFound() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> emailService.sendTemplateEmail(templateEmailRequest));
    }

    @Test
    void sendTemplateEmail_TemplateNotActive() {
        testTemplate.setActive(false);
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));

        assertThrows(IllegalArgumentException.class, () -> emailService.sendTemplateEmail(templateEmailRequest));
    }

    @Test
    void sendTestEmail_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(sendGridService.sendTestEmail(testEmail)).thenReturn("message-id-123");
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);
        when(emailResponseConverter.convertToResponse(testEmail)).thenReturn(EmailResponse.builder().build());

        EmailResponse result = emailService.sendTestEmail(testEmail.getId());

        assertNotNull(result);
        assertTrue(testEmail.getIsTestMail());
        assertEquals(EmailStatus.SENT, testEmail.getStatus());
        assertEquals("message-id-123", testEmail.getSendgridMessageId());
        assertNotNull(testEmail.getSentAt());
    }

    @Test
    void sendTestEmail_Failure() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(sendGridService.sendTestEmail(testEmail)).thenThrow(new RuntimeException("Send error"));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);
        when(emailResponseConverter.convertToResponse(testEmail)).thenReturn(EmailResponse.builder().build());

        EmailResponse result = emailService.sendTestEmail(testEmail.getId());

        assertNotNull(result);
        assertEquals(EmailStatus.FAILED, testEmail.getStatus());
        assertTrue(testEmail.getErrorMessage().contains("Test send failed"));
    }

    @Test
    void markAsQueued_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);

        emailService.markAsQueued(testEmail.getId());

        assertEquals(EmailStatus.QUEUED, testEmail.getStatus());
        verify(emailRepository).save(testEmail);
    }

    @Test
    void markAsSent_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);

        emailService.markAsSent(testEmail.getId(), "message-id-123");

        assertEquals(EmailStatus.SENT, testEmail.getStatus());
        assertEquals("message-id-123", testEmail.getSendgridMessageId());
        assertNotNull(testEmail.getSentAt());
        verify(emailRepository).save(testEmail);
    }

    @Test
    void markAsDelivered_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);

        emailService.markAsDelivered(testEmail.getId());

        assertEquals(EmailStatus.DELIVERED, testEmail.getStatus());
        assertNotNull(testEmail.getDeliveredAt());
        verify(emailRepository).save(testEmail);
    }

    @Test
    void markAsFailed_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);

        emailService.markAsFailed(testEmail.getId(), "Failed to send");

        assertEquals(EmailStatus.FAILED, testEmail.getStatus());
        assertEquals("Failed to send", testEmail.getErrorMessage());
        verify(emailRepository).save(testEmail);
    }

    @Test
    void markAsBounced_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        when(emailRepository.save(any(Email.class))).thenReturn(testEmail);

        emailService.markAsBounced(testEmail.getId());

        assertEquals(EmailStatus.BOUNCED, testEmail.getStatus());
        verify(emailRepository).save(testEmail);
    }

    @Test
    void findBySendgridMessageId_Found() {
        when(emailRepository.findBySendgridMessageId("message-id-123")).thenReturn(Optional.of(testEmail));

        Email result = emailService.findBySendgridMessageId("message-id-123");

        assertNotNull(result);
        assertEquals(testEmail, result);
    }

    @Test
    void findBySendgridMessageId_NotFound() {
        when(emailRepository.findBySendgridMessageId("message-id-123")).thenReturn(Optional.empty());

        Email result = emailService.findBySendgridMessageId("message-id-123");

        assertNull(result);
    }

    @Test
    void getEmailById_Success() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.of(testEmail));
        EmailDetailResponse expectedResponse = EmailDetailResponse.builder().build();
        when(emailDetailResponseConverter.convertToResponse(testEmail)).thenReturn(expectedResponse);

        EmailDetailResponse result = emailService.getEmailById(testEmail.getId());

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void getEmailById_NotFound() {
        when(emailRepository.findById(testEmail.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> emailService.getEmailById(testEmail.getId()));
    }

    @Test
    void getEmails_WithFilters() {
        Page<Email> emailPage = new PageImpl<>(List.of(testEmail));
        PagedResponse<EmailResponse> expectedResponse = PagedResponse.<EmailResponse>builder().build();
        
        when(emailRepository.findWithFilters(eq(EmailStatus.SENT), any(), any(), any(Pageable.class)))
            .thenReturn(emailPage);
        when(pagedResponseConverter.toPagedResponse(emailPage)).thenReturn(expectedResponse);

        PagedResponse<EmailResponse> result = emailService.getEmails(
            EmailStatus.SENT, LocalDateTime.now().minusDays(1), LocalDateTime.now(), 
            org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void getDashboardStats_Success() {
        when(emailRepository.count()).thenReturn(100L);
        when(emailRepository.countByStatus(EmailStatus.PENDING)).thenReturn(10L);
        when(emailRepository.countByStatus(EmailStatus.QUEUED)).thenReturn(20L);
        when(emailRepository.countByStatus(EmailStatus.SENT)).thenReturn(50L);
        when(emailRepository.countByStatus(EmailStatus.DELIVERED)).thenReturn(40L);
        when(emailRepository.countByStatus(EmailStatus.FAILED)).thenReturn(15L);
        when(emailRepository.countByStatus(EmailStatus.BOUNCED)).thenReturn(5L);

        DashboardStatsResponse result = emailService.getDashboardStats();

        assertNotNull(result);
        assertEquals(100L, result.getTotalEmails());
        assertEquals(10L, result.getPending());
        assertEquals(20L, result.getQueued());
        assertEquals(50L, result.getSent());
        assertEquals(40L, result.getDelivered());
        assertEquals(15L, result.getFailed());
        assertEquals(5L, result.getBounced());
    }

    @Test
    void serializeVariables_NullVariables() {
        String result = (String) ReflectionTestUtils.invokeMethod(emailService, "serializeVariables", (Object) null);
        assertNull(result);
    }

    @Test
    void serializeVariables_EmptyVariables() {
        String result = (String) ReflectionTestUtils.invokeMethod(emailService, "serializeVariables", new HashMap<>());
        assertNull(result);
    }

    @Test
    void serializeVariables_ValidVariables() throws JsonProcessingException {
        Map<String, String> variables = Map.of("name", "John", "email", "john@example.com");
        when(objectMapper.writeValueAsString(variables)).thenReturn("{\"name\":\"John\",\"email\":\"john@example.com\"}");

        String result = (String) ReflectionTestUtils.invokeMethod(emailService, "serializeVariables", variables);

        assertEquals("{\"name\":\"John\",\"email\":\"john@example.com\"}", result);
    }
}
