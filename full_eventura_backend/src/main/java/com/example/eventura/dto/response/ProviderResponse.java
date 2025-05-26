package com.example.eventura.dto.response;

import lombok.Data;

@Data
public class ProviderResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String serviceType;
    private String address;
    private String mobileNumber;
    private Boolean isVerified;
}