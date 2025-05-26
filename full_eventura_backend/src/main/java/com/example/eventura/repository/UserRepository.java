package com.example.eventura.repository;

import com.example.eventura.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByMobileNumber(String mobileNumber);
}