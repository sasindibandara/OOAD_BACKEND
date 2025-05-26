package com.example.eventura.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long requestId;
    private Integer rating;
    private String comment;
}