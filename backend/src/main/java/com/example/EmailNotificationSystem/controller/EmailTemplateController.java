package com.example.EmailNotificationSystem.controller;

import com.example.EmailNotificationSystem.dto.*;
import com.example.EmailNotificationSystem.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request){
        TemplateResponse response = emailTemplateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Template created" , response));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable UUID id , @RequestBody UpdateTemplateRequest request){
        TemplateResponse response = emailTemplateService.updateTemplate(id , request);
        return ResponseEntity.ok(ApiResponse.ok("Template created" , response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable UUID id) {
        TemplateResponse response = emailTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.ok("Template retrieved" , response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAllTemplates(
        @RequestParam(defaultValue = "false") boolean activeOnly){

        List<TemplateResponse> response = activeOnly ? emailTemplateService.getAllActiveTemplates():emailTemplateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.ok("Templates retrieved" , response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        emailTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.ok("Template deactivated" , null));
    }

}
