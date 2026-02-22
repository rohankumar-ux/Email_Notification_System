package com.example.EmailNotificationSystem.controller;

import com.example.EmailNotificationSystem.model.Email;
import com.example.EmailNotificationSystem.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class SendGridWebhookController {

    private final EmailService emailService;

    @PostMapping("/sendgrid")
    public ResponseEntity<Void> handleSendGridEvents(
            @RequestBody List<Map<String, Object>> events) {

        log.info("Received {} SendGrid webhook event(s)", events.size());

        for (Map<String, Object> event : events) {
            processEvent(event);
        }

        return ResponseEntity.ok().build();
    }

    private void processEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        String messageId = (String) event.get("sg_message_id");

        if (messageId == null) {
            log.debug("Skipping webhook event with no message ID: type={}", eventType);
            return;
        }

        // Strip SendGrid filter suffix
        // e.g. JaKjVHNgSVuVmoRWg6SjKA.recvd-6cfbc69475-ktzpb-1-699A0326-8 → JaKjVHNgSVuVmoRWg6SjKA
        String cleanMessageId = messageId.contains(".")
                ? messageId.substring(0, messageId.indexOf('.'))
                : messageId;
        log.info("Processing SendGrid event: type={}, messageId={}", eventType, cleanMessageId);

        Email email = emailService.findBySendgridMessageId(cleanMessageId);
        if (email == null) {
            log.warn("No email record found for messageId={} — event type={}", cleanMessageId, eventType);
            return;
        }

        switch (eventType) {
            case "delivered" -> emailService.markAsDelivered(email.getId());
            case "bounce" -> emailService.markAsBounced(email.getId());
            case "dropped", "spamreport" ->
                    emailService.markAsFailed(email.getId(),
                            "SendGrid event: " + eventType);
            default -> log.debug("Ignored SendGrid event type: {}", eventType);
        }
    }
}
