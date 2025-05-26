package com.example.eventura.repository;

import com.example.eventura.entity.Pitch;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PitchRepository extends JpaRepository<Pitch, Long> {
    Page<Pitch> findByRequest(ServiceRequest request, Pageable pageable);
    Page<Pitch> findByProvider(User provider, Pageable pageable);
}