package com.example.eventura.dto.request;

import com.example.eventura.entity.Pitch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PitchRequest {
    private Long requestId;
    private String pitchDetails;
    private Double proposedPrice;
    private Pitch.Status status;
}