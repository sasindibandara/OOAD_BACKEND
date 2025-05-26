package com.example.eventura.repository;

import com.example.eventura.entity.ServiceProvider;
import com.example.eventura.entity.VerificationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, Long>,
        JpaSpecificationExecutor<VerificationDocument> {
    Page<VerificationDocument> findByProvider(ServiceProvider provider, Pageable pageable);
}