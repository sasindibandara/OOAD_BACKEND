package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class
PortfolioResponse {
    private Long id;
    private Long providerId;
    private String title;
    private String description;
    private String imageUrl;
    private String projectDate;
    private String eventType;
    private String status;
    private LocalDateTime createdAt;
}