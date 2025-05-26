package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationDocumentResponse {
    private Long id;
    private Long providerId;
    private String documentType;
    private String documentUrl;
    private String status;
    private LocalDateTime createdAt;
}