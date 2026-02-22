package com.example.EmailNotificationSystem.model;

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
@Table(name = "email_templates")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false , unique = true)
    private String name;

    @Column(nullable = false)
    private String subject;

    @Column(name = "html_body" , columnDefinition = "TEXT")
    private String htmlBody;

    @Column(name = "variable_keys" , columnDefinition = "TEXT")
    private String variableKeys;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public List<String> getVariableKeyList(){
        if(variableKeys == null || variableKeys.isBlank()){
            return List.of();
        }
        return List.of(variableKeys.split(","));
    }
}
