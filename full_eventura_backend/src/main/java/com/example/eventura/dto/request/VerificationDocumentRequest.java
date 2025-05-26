package com.example.eventura.dto.request;

import lombok.Data;

@Data
public class VerificationDocumentRequest {
    private String documentType;
    private String documentUrl;
}