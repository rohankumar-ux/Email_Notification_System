package com.example.EmailNotificationSystem.repository;

import com.example.EmailNotificationSystem.model.EmailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailAttachmentRepository extends JpaRepository<EmailAttachment, UUID> {
    List<EmailAttachment> findByEmailId(UUID emailId);
}
