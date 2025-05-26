package com.example.eventura.repository;

import com.example.eventura.entity.DirectConnection;
import com.example.eventura.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectConnectionRepository extends JpaRepository<DirectConnection, Long> {
    Page<DirectConnection> findByClient(User client, Pageable pageable);
    Page<DirectConnection> findByProvider(User provider, Pageable pageable);
}