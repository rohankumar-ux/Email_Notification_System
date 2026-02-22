package com.example.EmailNotificationSystem.repository;

import com.example.EmailNotificationSystem.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate , UUID> {
    
    Optional<EmailTemplate> findByName(String name);
    
    List<EmailTemplate> findAllByActiveTrue();

    boolean existsByName(String name);
}
