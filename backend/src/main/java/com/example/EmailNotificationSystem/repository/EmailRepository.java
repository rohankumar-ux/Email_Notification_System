package com.example.EmailNotificationSystem.repository;

import com.example.EmailNotificationSystem.enums.EmailStatus;
import com.example.EmailNotificationSystem.model.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<Email , UUID> {

    Optional<Email> findById(UUID emailId);

    Optional<Email> findBySendgridMessageId(String sendgridMessageId);
    
    Page<Email> findByStatus(EmailStatus status , Pageable pageable);

    Page<Email> findByStatusAndCreatedAtBetween(EmailStatus status , LocalDateTime from , LocalDateTime to , Pageable pageable);

    Page<Email> findByCreatedAtBetween(LocalDateTime from , LocalDateTime to , Pageable pageable);

    @Query("SELECT count(e) FROM Email e WHERE e.status = :status")
    long countByStatus(@Param("status") EmailStatus status);
    
    @Query("SELECT count(e) FROM Email e WHERE e.status = :status AND e.createdAt BETWEEN :from AND :to")
    long countByStatusAndCreatedAtBetween(@Param("status") EmailStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT count(e) FROM Email e WHERE e.createdAt BETWEEN :from AND :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            Select e from Email e
            Where (:status is null or e.status = :status)
            AND (:from is null or e.createdAt >= :from)
            AND (:to is null or e.createdAt <= :to)
            """)
    Page<Email> findWithFilters(@Param("status") EmailStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to , Pageable pageable);
}

