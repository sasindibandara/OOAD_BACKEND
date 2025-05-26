package com.example.eventura.repository;

import com.example.eventura.entity.Portfolio;
import com.example.eventura.entity.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, JpaSpecificationExecutor<Portfolio> {
    Page<Portfolio> findByProvider(ServiceProvider provider, Pageable pageable);
}