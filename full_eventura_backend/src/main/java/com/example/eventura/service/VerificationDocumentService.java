package com.example.eventura.service;

import com.example.eventura.dto.request.VerificationDocumentRequest;
import com.example.eventura.dto.response.VerificationDocumentResponse;
import com.example.eventura.entity.ServiceProvider;
import com.example.eventura.entity.VerificationDocument;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.repository.ServiceProviderRepository;
import com.example.eventura.repository.VerificationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationDocumentService {

    private final VerificationDocumentRepository verificationDocumentRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public VerificationDocumentResponse uploadDocument(Long providerId, VerificationDocumentRequest request) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        VerificationDocument document = new VerificationDocument();
        document.setProvider(provider);
        document.setDocumentType(request.getDocumentType());
        document.setDocumentUrl(request.getDocumentUrl());
        document.setStatus(VerificationDocument.Status.PENDING);

        VerificationDocument savedDocument = verificationDocumentRepository.save(document);

        return convertToResponse(savedDocument);
    }

    public Page<VerificationDocumentResponse> getDocuments(Long providerId, Pageable pageable) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        return verificationDocumentRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }

    public Page<VerificationDocumentResponse> getAllDocuments(String status, Pageable pageable) {
        Page<VerificationDocument> documents;

        if (status != null && !status.isEmpty()) {
            try {
                VerificationDocument.Status documentStatus = VerificationDocument.Status.valueOf(status.toUpperCase());

                // Create specification for status filtering
                Specification<VerificationDocument> spec = (root, query, cb) ->
                        cb.equal(root.get("status"), documentStatus);

                documents = verificationDocumentRepository.findAll(spec, pageable);
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Invalid status: " + status);
            }
        } else {
            documents = verificationDocumentRepository.findAll(pageable);
        }

        return documents.map(this::convertToResponse);
    }

    private VerificationDocumentResponse convertToResponse(VerificationDocument document) {
        VerificationDocumentResponse response = new VerificationDocumentResponse();
        response.setId(document.getId());
        response.setProviderId(document.getProvider().getId());
        response.setDocumentType(document.getDocumentType());
        response.setDocumentUrl(document.getDocumentUrl());
        response.setStatus(document.getStatus().name());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }
}