package com.example.eventura.repository;

import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Page<ServiceRequest> findByClient(User client, Pageable pageable);
    Page<ServiceRequest> findByServiceType(String serviceType, Pageable pageable);
}