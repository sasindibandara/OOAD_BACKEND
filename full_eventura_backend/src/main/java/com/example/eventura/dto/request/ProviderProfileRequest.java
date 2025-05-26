package com.example.eventura.dto.request;

import lombok.Data;

@Data
public class ProviderProfileRequest {
    private String companyName;
    private String serviceType;
    private String address;
    private String mobileNumber;
}