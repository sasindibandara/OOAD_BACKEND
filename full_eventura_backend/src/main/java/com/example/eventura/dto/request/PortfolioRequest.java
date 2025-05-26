package com.example.eventura.dto.request;

import lombok.Data;

@Data
public class PortfolioRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String projectDate;
    private String eventType;
}