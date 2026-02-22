package com.example.EmailNotificationSystem.service;

import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.EmailNotificationSystem.converter.TemplateResponseConverter;
import com.example.EmailNotificationSystem.dto.CreateTemplateRequest;
import com.example.EmailNotificationSystem.dto.TemplateResponse;
import com.example.EmailNotificationSystem.dto.UpdateTemplateRequest;
import com.example.EmailNotificationSystem.exception.DuplicateResourceException;
import com.example.EmailNotificationSystem.exception.ResourceNotFoundException;
import com.example.EmailNotificationSystem.model.EmailTemplate;
import com.example.EmailNotificationSystem.repository.EmailTemplateRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateResponseConverter templateResponseConverter;

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {

        if (emailTemplateRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Template with name " + request.getName() + " already exists");
        }

        EmailTemplate emailTemplate = EmailTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .htmlBody(request.getHtmlBody())
                .variableKeys(request.getVariableKeys() != null ? String.join(",", request.getVariableKeys()) : null)
                .active(true)
                .build();

        emailTemplate = emailTemplateRepository.save(emailTemplate);
        log.info("Template created: id={}, name={}", emailTemplate.getId(), emailTemplate.getName());

        return templateResponseConverter.convertToResponse(emailTemplate);
    }

    @Transactional
    public TemplateResponse updateTemplate(UUID id, UpdateTemplateRequest request) {
        EmailTemplate template = getTemplate(id);

        if (request.getName() != null && !request.getName().equals(template.getName())) {
            if (emailTemplateRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Template with name " + request.getName() + " already exists");
            }
            template.setName(request.getName());
        }

        if(request.getSubject() != null){
            template.setSubject(request.getSubject());
        }

        if(request.getHtmlBody() != null){
            template.setHtmlBody(request.getHtmlBody());
        }

        if(request.getVariableKeys() != null){
            template.setVariableKeys(String.join("," , request.getVariableKeys()));
        }

        if(request.getActive() != null){
            template.setActive(request.getActive());
        }

        template = emailTemplateRepository.save(template);
        log.info("Template updated: id={}", template.getId());

        return templateResponseConverter.convertToResponse(template);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(UUID id){
        return templateResponseConverter.convertToResponse(getTemplate(id));
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllActiveTemplates(){
        return emailTemplateRepository.findAllByActiveTrue().stream()
            .map(templateResponseConverter::convertToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates(){
        return emailTemplateRepository.findAll().stream()
            .map(templateResponseConverter::convertToResponse)
            .toList();
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        EmailTemplate template = getTemplate(id);
        template.setActive(false);
        emailTemplateRepository.save(template);
    }

    private EmailTemplate getTemplate(UUID id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id=" + id));
    }
}
