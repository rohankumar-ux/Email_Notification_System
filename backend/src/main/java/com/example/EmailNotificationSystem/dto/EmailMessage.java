package com.example.EmailNotificationSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Lightweight message published to RabbitMQ.
 * Only the email ID is sent — the consumer fetches full data from DB.
 * This avoids stale data issues if status changes between publish and consume.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    private UUID emailId;
}
