package com.example.eventura.repository;

import com.example.eventura.entity.ServiceProvider;
import com.example.eventura.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    ServiceProvider findByUser(User user);
}