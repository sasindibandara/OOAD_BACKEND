package com.example.eventura.repository;

import com.example.eventura.entity.Review;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProvider(User provider, Pageable pageable);
    Page<Review> findByRequest(ServiceRequest request, Pageable pageable);
}