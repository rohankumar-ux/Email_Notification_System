package com.example.EmailNotificationSystem.model;

import com.example.EmailNotificationSystem.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="emails")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "from_email" , nullable = false)
    private String fromEmail;

    @Column(name = "is_test_mail")
    @Builder.Default
    private Boolean isTestMail = false;

    @Column(name = "to_emails",nullable = false,columnDefinition = "TEXT")
    private String toEmails;

    @Column(nullable = false)
    private String subject;

    @Column(name = "body" , columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_html")
    @Builder.Default
    private boolean html = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private EmailTemplate emailTemplate;

    @Column(name = "template_variables",columnDefinition = "TEXT")
    private String templateVariables;

    /* SendGrid Message ID returned after a successful send */
    @Column(name = "sendgrid_message_id")
    private String sendgridMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "error_message" , columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public List<String> getToEmailList(){
        if(toEmails == null || toEmails.isBlank()){
            return List.of();
        }
        return List.of(toEmails.split(","));
    }
}
