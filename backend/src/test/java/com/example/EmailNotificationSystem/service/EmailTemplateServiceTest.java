package com.example.EmailNotificationSystem.service;

import com.example.EmailNotificationSystem.converter.TemplateResponseConverter;
import com.example.EmailNotificationSystem.dto.CreateTemplateRequest;
import com.example.EmailNotificationSystem.dto.TemplateResponse;
import com.example.EmailNotificationSystem.dto.UpdateTemplateRequest;
import com.example.EmailNotificationSystem.exception.DuplicateResourceException;
import com.example.EmailNotificationSystem.exception.ResourceNotFoundException;
import com.example.EmailNotificationSystem.model.EmailTemplate;
import com.example.EmailNotificationSystem.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    
    @Mock
    private TemplateResponseConverter templateResponseConverter;

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    private EmailTemplate testTemplate;
    private CreateTemplateRequest createRequest;
    private UpdateTemplateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testTemplate = EmailTemplate.builder()
            .id(UUID.randomUUID())
            .name("Test Template")
            .subject("Test Subject")
            .htmlBody("<html>Test Body</html>")
            .variableKeys("name,email")
            .active(true)
            .build();

        createRequest = new CreateTemplateRequest();
        createRequest.setName("New Template");
        createRequest.setSubject("New Subject");
        createRequest.setHtmlBody("<html>New Body</html>");
        createRequest.setVariableKeys(List.of("name", "email"));

        updateRequest = new UpdateTemplateRequest();
        updateRequest.setName("Updated Template");
        updateRequest.setSubject("Updated Subject");
        updateRequest.setHtmlBody("<html>Updated Body</html>");
        updateRequest.setVariableKeys(List.of("name", "email", "phone"));
        updateRequest.setActive(false);
    }

    @Test
    void createTemplate_Success() {
        when(emailTemplateRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.createTemplate(createRequest);

        assertNotNull(result);
        verify(emailTemplateRepository).existsByName(createRequest.getName());
        verify(emailTemplateRepository).save(any(EmailTemplate.class));
        verify(templateResponseConverter).convertToResponse(testTemplate);
    }

    @Test
    void createTemplate_DuplicateName() {
        when(emailTemplateRepository.existsByName(createRequest.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> emailTemplateService.createTemplate(createRequest));
        verify(emailTemplateRepository).existsByName(createRequest.getName());
        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void createTemplate_NullVariableKeys() {
        createRequest.setVariableKeys(null);
        when(emailTemplateRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.createTemplate(createRequest);

        assertNotNull(result);
        verify(emailTemplateRepository).save(argThat(template -> template.getVariableKeys() == null));
    }

    @Test
    void updateTemplate_Success() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.updateTemplate(testTemplate.getId(), updateRequest);

        assertNotNull(result);
        assertEquals(updateRequest.getName(), testTemplate.getName());
        assertEquals(updateRequest.getSubject(), testTemplate.getSubject());
        assertEquals(updateRequest.getHtmlBody(), testTemplate.getHtmlBody());
        assertEquals("name,email,phone", testTemplate.getVariableKeys());
        assertEquals(updateRequest.getActive().booleanValue(), testTemplate.isActive());
        verify(emailTemplateRepository).save(testTemplate);
    }

    @Test
    void updateTemplate_NotFound() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> emailTemplateService.updateTemplate(testTemplate.getId(), updateRequest));
    }

    @Test
    void updateTemplate_DuplicateName() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.existsByName(updateRequest.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> emailTemplateService.updateTemplate(testTemplate.getId(), updateRequest));
    }

    @Test
    void updateTemplate_SameName() {
        updateRequest.setName(testTemplate.getName());
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.updateTemplate(testTemplate.getId(), updateRequest);

        assertNotNull(result);
        verify(emailTemplateRepository, never()).existsByName(any());
        verify(emailTemplateRepository).save(testTemplate);
    }

    @Test
    void updateTemplate_NullFields() {
        UpdateTemplateRequest partialUpdate = new UpdateTemplateRequest();
        partialUpdate.setSubject("Partial Update");

        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.updateTemplate(testTemplate.getId(), partialUpdate);

        assertNotNull(result);
        assertEquals("Partial Update", testTemplate.getSubject());
        assertEquals("Test Template", testTemplate.getName());
        assertEquals("<html>Test Body</html>", testTemplate.getHtmlBody());
        verify(emailTemplateRepository).save(testTemplate);
    }

    @Test
    void getTemplateById_Success() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        TemplateResponse result = emailTemplateService.getTemplateById(testTemplate.getId());

        assertNotNull(result);
        verify(emailTemplateRepository).findById(testTemplate.getId());
        verify(templateResponseConverter).convertToResponse(testTemplate);
    }

    @Test
    void getTemplateById_NotFound() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> emailTemplateService.getTemplateById(testTemplate.getId()));
    }

    @Test
    void getAllActiveTemplates_Success() {
        List<EmailTemplate> activeTemplates = List.of(testTemplate);
        when(emailTemplateRepository.findAllByActiveTrue()).thenReturn(activeTemplates);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        List<TemplateResponse> result = emailTemplateService.getAllActiveTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(emailTemplateRepository).findAllByActiveTrue();
        verify(templateResponseConverter).convertToResponse(testTemplate);
    }

    @Test
    void getAllTemplates_Success() {
        List<EmailTemplate> allTemplates = List.of(testTemplate);
        when(emailTemplateRepository.findAll()).thenReturn(allTemplates);
        when(templateResponseConverter.convertToResponse(testTemplate)).thenReturn(TemplateResponse.builder().build());

        List<TemplateResponse> result = emailTemplateService.getAllTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(emailTemplateRepository).findAll();
        verify(templateResponseConverter).convertToResponse(testTemplate);
    }

    @Test
    void deleteTemplate_Success() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);

        emailTemplateService.deleteTemplate(testTemplate.getId());

        assertFalse(testTemplate.isActive());
        verify(emailTemplateRepository).findById(testTemplate.getId());
        verify(emailTemplateRepository).save(testTemplate);
    }

    @Test
    void deleteTemplate_NotFound() {
        when(emailTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> emailTemplateService.deleteTemplate(testTemplate.getId()));
    }
}
