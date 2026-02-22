package com.example.EmailNotificationSystem.messaging;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.EmailNotificationSystem.dto.EmailMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.email}")
    private String emailExchange;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    
    public void sendEmail(UUID emailId) {
        EmailMessage message = EmailMessage.builder()
                .emailId(emailId)
                .build();

        log.info("Publishing email message for emailId={} to exchange={}", emailId, emailExchange);

        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, message);

        log.debug("Email message published successfully for emailId={}", emailId);

    }
}