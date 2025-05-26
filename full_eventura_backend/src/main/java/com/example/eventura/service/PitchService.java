package com.example.eventura.service;

import com.example.eventura.dto.request.PitchRequest;
import com.example.eventura.dto.response.PitchResponse;
import com.example.eventura.entity.Pitch;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.PitchRepository;
import com.example.eventura.repository.ServiceRequestRepository;
import com.example.eventura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PitchService {

    private final PitchRepository pitchRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final NotificationService notificationService;

    public PitchResponse createPitch(Long providerId, PitchRequest request) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (!provider.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can create pitches");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        Pitch pitch = new Pitch();
        pitch.setRequest(serviceRequest);
        pitch.setProvider(provider);
        pitch.setMessage(request.getPitchDetails());
        pitch.setProposedPrice(request.getProposedPrice());
        pitch.setStatus(Pitch.Status.PENDING);

        Pitch savedPitch = pitchRepository.save(pitch);

        // Notify the client
        User client = serviceRequest.getClient();
        String message = String.format("New pitch from %s %s for your service request: %s",
                provider.getFirstName(), provider.getLastName(), serviceRequest.getTitle());
        notificationService.createNotification(client, message);

        return convertToResponse(savedPitch);
    }

    public Page<PitchResponse> getMyPitches(Long providerId, Pageable pageable) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (!provider.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can view their own pitches");
        }

        return pitchRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }

    public PitchResponse getPitch(Long pitchId) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found"));
        return convertToResponse(pitch);
    }

    public Page<PitchResponse> getPitchesForRequest(Long requestId, Pageable pageable) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        return pitchRepository.findByRequest(serviceRequest, pageable)
                .map(this::convertToResponse);
    }

    public PitchResponse updatePitchStatus(Long pitchId, Long userId, Pitch.Status status) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found"));

        // Check if the user is the client who owns the service request
        ServiceRequest serviceRequest = pitch.getRequest();
        if (!serviceRequest.getClient().getId().equals(userId)) {
            throw new UnauthorizedException("Only the client who created the service request can update pitch status");
        }

        pitch.setStatus(status);
        Pitch updatedPitch = pitchRepository.save(pitch);

        // Notify the provider
        User provider = pitch.getProvider();
        String message = String.format("Your pitch for service request: %s has been marked as %s",
                serviceRequest.getTitle(), status);
        notificationService.createNotification(provider, message);

        return convertToResponse(updatedPitch);
    }

    public void deletePitch(Long pitchId, Long userId) {
        Pitch pitch = pitchRepository.findById(pitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Pitch not found"));

        // Check if the user is the provider who created the pitch
        if (!pitch.getProvider().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this pitch");
        }

        pitchRepository.delete(pitch);

        // Optionally notify the client
        User client = pitch.getRequest().getClient();
        String message = String.format("Pitch from %s %s for your service request: %s has been withdrawn",
                pitch.getProvider().getFirstName(), pitch.getProvider().getLastName(),
                pitch.getRequest().getTitle());
        notificationService.createNotification(client, message);
    }

    private PitchResponse convertToResponse(Pitch pitch) {
        PitchResponse response = new PitchResponse();
        response.setId(pitch.getId());
        response.setRequestId(pitch.getRequest().getId());
        response.setProviderId(pitch.getProvider().getId());
        response.setPitchDetails(pitch.getMessage());
        response.setProposedPrice(pitch.getProposedPrice());
        response.setCreatedAt(pitch.getCreatedAt());
        response.setStatus(pitch.getStatus());
        return response;
    }
}