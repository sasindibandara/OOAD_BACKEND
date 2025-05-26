package com.example.eventura.service;

import com.example.eventura.dto.request.ReviewRequest;
import com.example.eventura.dto.response.ReviewResponse;
import com.example.eventura.entity.Review;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.ReviewRepository;
import com.example.eventura.repository.ServiceRequestRepository;
import com.example.eventura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ReviewResponse createReview(Long clientId, ReviewRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can create reviews");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        if (!serviceRequest.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("Not authorized to review this request");
        }

        if (serviceRequest.getAssignedProvider() == null) {
            throw new ResourceNotFoundException("No provider assigned to this request");
        }

        Review review = new Review();
        review.setRequest(serviceRequest);
        review.setClient(client);
        review.setProvider(serviceRequest.getAssignedProvider());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        return convertToResponse(savedReview);
    }

    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return convertToResponse(review);
    }

    public Page<ReviewResponse> getReviewsForProvider(Long providerId, Pageable pageable) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        return reviewRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }

    public ReviewResponse updateReviewStatus(Long reviewId, String status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Assuming status is for moderation (e.g., APPROVED, HIDDEN)
        // review.setStatus(status); // If Review had a status field
        Review updatedReview = reviewRepository.save(review);

        return convertToResponse(updatedReview);
    }

    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRequestId(review.getRequest().getId());
        response.setClientId(review.getClient().getId());
        response.setProviderId(review.getProvider().getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}