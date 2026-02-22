package com.example.EmailNotificationSystem.converter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.example.EmailNotificationSystem.dto.EmailResponse;
import com.example.EmailNotificationSystem.dto.PagedResponse;
import com.example.EmailNotificationSystem.model.Email;

@Component
public class PagedResponseConverter {
    
    private final EmailResponseConverter emailResponseConverter;
    
    public PagedResponseConverter(EmailResponseConverter emailResponseConverter) {
        this.emailResponseConverter = emailResponseConverter;
    }
    
    public PagedResponse<EmailResponse> toPagedResponse(Page<Email> page){
        return PagedResponse.<EmailResponse>builder()
            .content(page.getContent().stream().map(emailResponseConverter::convertToResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
