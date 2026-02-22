package com.example.EmailNotificationSystem.enums;

/**
 * Lifecycle states for an email record.
 * Transition flow: PENDING → QUEUED → SENT → DELIVERED / FAILED / BOUNCED
 *
 * PENDING  - Email record created, not yet pushed to queue
 * QUEUED   - Message published to RabbitMQ, awaiting consumer pickup
 * SENT     - SendGrid API accepted the send request (2xx response)
 * DELIVERED- Confirmed via SendGrid webhook (delivered event)
 * FAILED   - Either queue publish failed, SendGrid rejected it, or drop event received
 * BOUNCED  - SendGrid reported a bounce event via webhook
 */
public enum EmailStatus {
    PENDING,
    QUEUED,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED
}
