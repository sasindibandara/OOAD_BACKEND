package com.example.eventura.controller;

import com.example.eventura.dto.request.ReviewRequest;
import com.example.eventura.dto.response.ReviewResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> createReview(@RequestBody ReviewRequest request,
                                                       @RequestHeader("Authorization") String authHeader) {
        Long clientId = getUserIdFromToken(authHeader);
        return new ResponseEntity<>(reviewService.createReview(clientId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReview(reviewId));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsForProvider(@PathVariable Long providerId,
                                                                      Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsForProvider(providerId, pageable));
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
}