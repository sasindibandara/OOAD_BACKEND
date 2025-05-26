package com.example.eventura.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ServiceRequestResponse {
    private Long id;
    private Long clientId;
    private String title;
    private String eventName;
    private LocalDate eventDate;
    private String location;
    private String serviceType;
    private String description;
    private Double budget;
    private String status;
    private Long assignedProviderId;
    private LocalDateTime createdAt;
}