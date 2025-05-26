package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long requestId;
    private Long clientId;
    private Long providerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}