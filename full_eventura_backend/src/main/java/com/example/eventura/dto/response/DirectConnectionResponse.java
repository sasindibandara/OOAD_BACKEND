package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DirectConnectionResponse {
    private Long id;
    private Long clientId;
    private Long providerId;
    private String eventDetails;
    private String proposedDate;
    private String status;
    private LocalDateTime createdAt;
}