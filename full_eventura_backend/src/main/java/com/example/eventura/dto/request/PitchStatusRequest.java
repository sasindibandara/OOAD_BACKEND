package com.example.eventura.dto.request;

import com.example.eventura.entity.Pitch;
import lombok.Data;

@Data
public class PitchStatusRequest {
    private Pitch.Status status;
}