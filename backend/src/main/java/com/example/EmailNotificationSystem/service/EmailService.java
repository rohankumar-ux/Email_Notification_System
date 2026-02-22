package com.example.EmailNotificationSystem.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EmailNotificationSystem.converter.EmailDetailResponseConverter;
import com.example.EmailNotificationSystem.converter.EmailResponseConverter;
import com.example.EmailNotificationSystem.converter.PagedResponseConverter;
import com.example.EmailNotificationSystem.dto.DashboardStatsResponse;
import com.example.EmailNotificationSystem.dto.EmailDetailResponse;
import com.example.EmailNotificationSystem.dto.EmailResponse;
import com.example.EmailNotificationSystem.dto.PagedResponse;
import com.example.EmailNotificationSystem.dto.SendRawEmailRequest;
import com.example.EmailNotificationSystem.dto.SendTemplateEmailRequest;
import com.example.EmailNotificationSystem.enums.EmailStatus;
import com.example.EmailNotificationSystem.exception.ResourceNotFoundException;
import com.example.EmailNotificationSystem.messaging.EmailProducer;
import com.example.EmailNotificationSystem.model.Email;
import com.example.EmailNotificationSystem.model.EmailTemplate;
import com.example.EmailNotificationSystem.repository.EmailRepository;
import com.example.EmailNotificationSystem.repository.EmailTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailProducer emailProducer;

    private final EmailResponseConverter emailResponseConverter;
    private final EmailDetailResponseConverter emailDetailResponseConverter;
    private final PagedResponseConverter pagedResponseConverter;
    private final SendGridService sendGridService;

    private final ObjectMapper objectMapper;

    @Value("${sendgrid.from-email}")
    private String defaultFromEmail;


    @Transactional
    public EmailResponse sendRawEmail(SendRawEmailRequest request){
        Email email = Email.builder()
            .fromEmail(defaultFromEmail)
            .toEmails(String.join("," , request.getToEmails()))
            .subject(request.getSubject())
            .body(request.getBody())
            .html(request.isHtml())
            .status(EmailStatus.PENDING)
            .build();
        
        email = emailRepository.save(email);
        log.info("Raw email record created: id={}", email.getId());

        enqueueEmail(email);

        return emailResponseConverter.convertToResponse(email);
    }

    @Transactional
    public EmailResponse sendTemplateEmail(SendTemplateEmailRequest request){
        EmailTemplate template = emailTemplateRepository.findById(request.getTemplateId())
            .orElseThrow(() -> new ResourceNotFoundException("Template not found with id:" + request.getTemplateId()));

        if(!template.isActive()){
            throw new IllegalArgumentException("Template " + template.getName() + " is not active");
        }

        String variableJson = serializeVariables(request.getVariables());

        Email email = Email.builder()
                .fromEmail(defaultFromEmail)
                .toEmails(String.join("," , request.getToEmails()))
                .subject(template.getSubject())
                .emailTemplate(template)
                .templateVariables(variableJson)
                .status(EmailStatus.PENDING)
                .build();
        
        email = emailRepository.save(email);
        log.info("Template email record created: id={}, templateId={}", email.getId(), template.getId());

        enqueueEmail(email);

        return emailResponseConverter.convertToResponse(email);
    }

    @Transactional
    public EmailResponse sendTestEmail(UUID emailId) {
        Email email = getEmail(emailId);
        email.setIsTestMail(true);
        emailRepository.save(email);

        try {
            String messageId = sendGridService.sendTestEmail(email);
            email.setStatus(EmailStatus.SENT);
            email.setSendgridMessageId(messageId);
            email.setSentAt(LocalDateTime.now());
            log.info("Test email sent: id={}, to={}", emailId, email.getFromEmail());
        } catch (Exception ex) {
            email.setStatus(EmailStatus.FAILED);
            email.setErrorMessage("Test send failed: " + ex.getMessage());
            log.error("Test email failed: id={}", emailId, ex);
        }

        email = emailRepository.save(email);
        return emailResponseConverter.convertToResponse(email);
    }
    @Transactional
    public void markAsQueued(UUID emailId){
        Email email = getEmail(emailId);
        email.setStatus(EmailStatus.QUEUED);
        emailRepository.save(email);
    }

    @Transactional
    public void markAsSent(UUID emailId , String sendgridMessageId){
        Email email = getEmail(emailId);
        email.setStatus(EmailStatus.SENT);
        email.setSendgridMessageId(sendgridMessageId);
        email.setSentAt(LocalDateTime.now());
        emailRepository.save(email);
        log.info("Email marked as SENT: id={}, messageId={}", emailId, sendgridMessageId);

    }

     @Transactional
    public void markAsDelivered(UUID emailId){
        Email email = getEmail(emailId);
        email.setStatus(EmailStatus.DELIVERED);
        email.setDeliveredAt(LocalDateTime.now());
        emailRepository.save(email);
         log.info("Email marked as DELIVERED: id={}", emailId);

     }

     @Transactional
    public void markAsFailed(UUID emailId , String errorMessage){
        Email email = getEmail(emailId);
        email.setStatus(EmailStatus.FAILED);
        email.setErrorMessage(errorMessage);
        emailRepository.save(email);
        log.warn("Email marked as FAILED: id={}, reason={}", emailId, errorMessage);

     }

    @Transactional
    public void markAsBounced(UUID emailId){
        Email email = getEmail(emailId);
        email.setStatus(EmailStatus.BOUNCED);
        emailRepository.save(email);
        log.warn("Email marked as BOUNCED: id={}", emailId);
    }

    public Email findBySendgridMessageId(String sendgridMessageId) {
        return emailRepository.findBySendgridMessageId(sendgridMessageId)
                .orElse(null);
    }
    
    public EmailDetailResponse getEmailById(UUID emailId){
        Email email = getEmail(emailId);
        return emailDetailResponseConverter.convertToResponse(email);
    }

    public PagedResponse<EmailResponse> getEmails(EmailStatus status , LocalDateTime from , LocalDateTime to , Pageable pageable){
       
        Page<Email> page = emailRepository.findWithFilters(status, from, to , pageable);

       return pagedResponseConverter.toPagedResponse(page);
        
    }

    public DashboardStatsResponse getDashboardStats(){
        return DashboardStatsResponse.builder()
            .totalEmails(emailRepository.count())
            .pending(emailRepository.countByStatus(EmailStatus.PENDING))
            .queued(emailRepository.countByStatus(EmailStatus.QUEUED))
            .sent(emailRepository.countByStatus(EmailStatus.SENT))
            .delivered(emailRepository.countByStatus(EmailStatus.DELIVERED))
            .failed(emailRepository.countByStatus(EmailStatus.FAILED))
            .bounced(emailRepository.countByStatus(EmailStatus.BOUNCED))
            .build();
    }

    private Email getEmail(UUID id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found with id=" + id));
    }

    private void enqueueEmail(Email email){
        try{
            emailProducer.sendEmail(email.getId());
            email.setStatus(EmailStatus.QUEUED);
            emailRepository.save(email);
        }catch(Exception ex){
            log.error("Failed to enqueue email id={}: {}", email.getId(), ex.getMessage());
            email.setStatus(EmailStatus.FAILED);
            email.setErrorMessage("Queue publish failed: " + ex.getMessage());
            emailRepository.save(email);
            throw ex;
        }
    }

    private String serializeVariables(Map<String , String> variables){
        if(variables == null || variables.isEmpty()){
            return null;
        }

        try{
            return objectMapper.writeValueAsString(variables);
        }catch(JsonProcessingException ex){
            log.warn("Failed to serialize template variables", ex);
            return null;
        }
    }
}
