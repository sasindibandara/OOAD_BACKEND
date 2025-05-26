package com.example.eventura.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    // Define JWT secret key as a bean
    @Bean
    public SecretKey jwtSecretKey() {
        // Generate a secure key for HS512 algorithm
        return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
    }

    // Define JWT expiration time as a bean (in milliseconds)
    @Bean
    public Long jwtExpirationMs() {
        // 1 day = 86,400,000 milliseconds
        return 86400000L;
    }
}