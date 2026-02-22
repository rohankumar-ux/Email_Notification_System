package com.example.EmailNotificationSystem.messaging;

import com.example.EmailNotificationSystem.dto.EmailMessage;
import com.example.EmailNotificationSystem.exception.ResourceNotFoundException;
import com.example.EmailNotificationSystem.model.Email;
import com.example.EmailNotificationSystem.repository.EmailRepository;
import com.example.EmailNotificationSystem.service.EmailService;
import com.example.EmailNotificationSystem.service.SendGridService;
import com.example.EmailNotificationSystem.service.TemplateRenderer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes email messages from the queue and processes them with SendGrid.
 *
 * Flow:
 * 1. Receive message with emailId
 * 2. Fetch full Email record from database
 * 3. Determine send mode (raw vs template)
 * 4. Call SendGrid API
 * 5. Mark email SENT (or FAILED on error)
 * 6. Manually ACK/NACK the RabbitMQ message
 *
 * Manual acknowledgement ensures messages are not lost if the process crashes
 * mid-flight.
 * On NACK without requeue after max retries, the message goes to the DLQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final SendGridService sendGridService;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.email}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeEmailMessage(
            EmailMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        UUID emailId = message.getEmailId();
        log.info("Received email message for emailId={}", emailId);

        try {
            Email email = findEmailWithRetry(emailId);
            
            String messageId = processEmail(email);
            emailService.markAsSent(emailId, messageId);

            channel.basicAck(deliveryTag, false);
            log.info("Email sent and ACKed: id={}", emailId);

        } catch (ResourceNotFoundException ex) {
            log.error("Email record missing for emailId={}, sending to DLQ", emailId);
            channel.basicNack(deliveryTag, false, false);

        } catch (Exception ex) {
            log.error("Failed to process email emailId={}: {}", emailId, ex.getMessage(), ex);
            emailService.markAsFailed(emailId, ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private String processEmail(Email email) {
        if (email.getEmailTemplate() != null) {
            return processTemplateEmail(email);
        } else {
            return sendGridService.sendRawEmail(email);
        }
    }

    private String processTemplateEmail(Email email) {
        Map<String, String> variables = deserializeVariables(email.getTemplateVariables());

        String renderedBody = templateRenderer.render(email.getEmailTemplate().getHtmlBody(), variables);
        email.setBody(renderedBody);
        email.setHtml(true);
        return sendGridService.sendRenderedTemplateMail(email);
    }

    private Email findEmailWithRetry(UUID emailId) throws ResourceNotFoundException {
        int maxRetries = 5;
        int retryDelayMs = 100;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return emailRepository.findById(emailId)
                        .orElseThrow(() -> new ResourceNotFoundException("Email not found: id=" + emailId));
            } catch (ResourceNotFoundException ex) {
                if (attempt == maxRetries) {
                    throw ex;
                }
                
                log.warn("Email not found on attempt {}/{}, retrying in {}ms: id={}", 
                        attempt, maxRetries, retryDelayMs, emailId);
                
                try {
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        
        throw new ResourceNotFoundException("Email not found after retries: id=" + emailId);
    }

    private Map<String, String> deserializeVariables(String json) {
        if (json == null || json.isBlank())
            return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception ex) {
            log.warn("Failed to deserialize template variables: {}", json, ex);
            return Map.of();
        }
    }
}