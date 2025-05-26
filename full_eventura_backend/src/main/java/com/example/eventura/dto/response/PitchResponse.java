package com.example.eventura.dto.response;

import com.example.eventura.entity.Pitch;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PitchResponse {
    private Long id;
    private Long requestId;
    private Long providerId;
    private String pitchDetails;
    private Double proposedPrice;
    private LocalDateTime createdAt;
    private Pitch.Status status;
}