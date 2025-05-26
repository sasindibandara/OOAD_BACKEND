package com.example.eventura.repository;

import com.example.eventura.entity.Payment;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByClient(User client, Pageable pageable);
    Page<Payment> findByProvider(User provider, Pageable pageable);
    Optional<Payment> findTopByRequestOrderByCreatedAtDesc(ServiceRequest request);
}