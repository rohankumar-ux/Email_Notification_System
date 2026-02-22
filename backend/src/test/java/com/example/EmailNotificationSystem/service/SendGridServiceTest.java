package com.example.EmailNotificationSystem.service;

import com.example.EmailNotificationSystem.exception.EmailSendException;
import com.example.EmailNotificationSystem.model.Email;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendGridServiceTest {

    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private SendGridService sendGridService;

    private Email testEmail;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sendGridService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendGridService, "fromName", "Test Sender");

        testEmail = Email.builder()
            .id(UUID.randomUUID())
            .fromEmail("sender@example.com")
            .toEmails("recipient1@example.com,recipient2@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .html(false)
            .build();
    }

    @Test
    void sendRawEmail_Success() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "message-id-123"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendRawEmail(testEmail);

        assertEquals("message-id-123", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendRawEmail_HtmlEmail() throws IOException {
        testEmail.setHtml(true);
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "message-id-123"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendRawEmail(testEmail);

        assertEquals("message-id-123", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendRawEmail_SendGridError() throws IOException {
        Response response = new Response();
        response.setStatusCode(400);
        response.setBody("Bad Request");

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThrows(EmailSendException.class, () -> sendGridService.sendRawEmail(testEmail));
    }

    @Test
    void sendRawEmail_NetworkError() throws IOException {
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        assertThrows(EmailSendException.class, () -> sendGridService.sendRawEmail(testEmail));
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendTemplateMail_Success() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "message-id-456"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        Map<String, String> variables = Map.of("name", "John", "email", "john@example.com");
        String messageId = sendGridService.sendTemplateMail(testEmail, variables);

        assertEquals("message-id-456", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendTemplateMail_NullVariables() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "message-id-456"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendTemplateMail(testEmail, null);

        assertEquals("message-id-456", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendRenderedTemplateMail_Success() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "message-id-789"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendRenderedTemplateMail(testEmail);

        assertEquals("message-id-789", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendTestEmail_Success() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "test-message-id"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendTestEmail(testEmail);

        assertEquals("test-message-id", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendTestEmail_HtmlEmail() throws IOException {
        testEmail.setHtml(true);
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(Map.of("X-Message-Id", "test-message-id"));

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendTestEmail(testEmail);

        assertEquals("test-message-id", messageId);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendTestEmail_SendGridError() throws IOException {
        Response response = new Response();
        response.setStatusCode(500);
        response.setBody("Internal Server Error");

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThrows(EmailSendException.class, () -> sendGridService.sendTestEmail(testEmail));
    }

    @Test
    void buildTestWrapper_PlainText() {
        String result = (String) ReflectionTestUtils.invokeMethod(
            sendGridService, "buildTestWrapper", "Test body", false);

        assertEquals("=== THIS IS A TEST EMAIL ===\n\nTest body", result);
    }

    @Test
    void buildTestWrapper_Html() {
        String result = (String) ReflectionTestUtils.invokeMethod(
            sendGridService, "buildTestWrapper", "<p>Test body</p>", true);

        assertTrue(result.contains("TEST EMAIL"));
        assertTrue(result.contains("This message was sent as a test"));
        assertTrue(result.contains("<p>Test body</p>"));
    }

    @Test
    void baseMail_CreatesCorrectFromAddress() {
        Mail result = (Mail) ReflectionTestUtils.invokeMethod(sendGridService, "baseMail");

        assertNotNull(result);
        assertEquals("sender@example.com", result.getFrom().getEmail());
        assertEquals("Test Sender", result.getFrom().getName());
    }

    @Test
    void sendMail_MissingMessageId() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        response.setHeaders(new HashMap<>());

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        String messageId = sendGridService.sendMail(new Mail(), UUID.randomUUID());

        assertNull(messageId);
    }

    @Test
    void sendMail_UnexpectedStatusCode() throws IOException {
        Response response = new Response();
        response.setStatusCode(100);
        response.setBody("Continue");

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThrows(EmailSendException.class, () -> sendGridService.sendMail(new Mail(), UUID.randomUUID()));
    }
}
