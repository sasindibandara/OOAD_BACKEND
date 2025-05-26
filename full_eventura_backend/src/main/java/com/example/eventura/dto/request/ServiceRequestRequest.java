package com.example.eventura.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceRequestRequest {
    private String title;
    private String eventName;
    private LocalDate eventDate;
    private String location;
    private String serviceType;
    private String description;
    private Double budget;
}