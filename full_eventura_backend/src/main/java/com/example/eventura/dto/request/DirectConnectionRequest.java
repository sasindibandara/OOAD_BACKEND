package com.example.eventura.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectConnectionRequest {
    private Long providerId;
    private String eventDetails;
    private String proposedDate;
    private String status;
}