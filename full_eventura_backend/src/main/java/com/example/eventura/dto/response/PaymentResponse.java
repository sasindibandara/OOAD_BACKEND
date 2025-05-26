package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long requestId;
    private Long clientId;
    private Long providerId;
    private Double amount;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
}