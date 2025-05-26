package com.example.eventura.service;

import com.example.eventura.dto.request.ServiceRequestRequest;
import com.example.eventura.dto.response.ServiceRequestResponse;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.ServiceRequestRepository;
import com.example.eventura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Add NotificationService dependency

    public ServiceRequestResponse createRequest(Long clientId, ServiceRequestRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can create requests");
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setClient(client);
        serviceRequest.setTitle(request.getTitle());
        serviceRequest.setEventName(request.getEventName());
        serviceRequest.setEventDate(request.getEventDate());
        serviceRequest.setLocation(request.getLocation());
        serviceRequest.setServiceType(request.getServiceType());
        serviceRequest.setDescription(request.getDescription());
        serviceRequest.setBudget(request.getBudget());
        serviceRequest.setStatus(ServiceRequest.Status.OPEN);

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToResponse(savedRequest);
    }

    public ServiceRequestResponse getRequest(Long requestId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        return convertToResponse(serviceRequest);
    }

    public Page<ServiceRequestResponse> getAllRequests(Pageable pageable, String serviceType) {
        Page<ServiceRequest> requests;
        if (serviceType != null && !serviceType.isEmpty()) {
            requests = serviceRequestRepository.findByServiceType(serviceType, pageable);
        } else {
            requests = serviceRequestRepository.findAll(pageable);
        }
        return requests.map(this::convertToResponse);
    }

    public Page<ServiceRequestResponse> getClientRequests(Long clientId, Pageable pageable) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("User is not a client");
        }

        Page<ServiceRequest> requests = serviceRequestRepository.findByClient(client, pageable);
        return requests.map(this::convertToResponse);
    }

    public ServiceRequestResponse assignProvider(Long requestId, Long providerId, Long clientId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can assign providers");
        }

        if (!provider.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Assigned user is not a provider");
        }

        if (!serviceRequest.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("Client does not own this request");
        }

        if (!serviceRequest.getStatus().equals(ServiceRequest.Status.OPEN)) {
            throw new IllegalStateException("Request must be in OPEN status to assign a provider");
        }

        serviceRequest.setAssignedProvider(provider);
        serviceRequest.setStatus(ServiceRequest.Status.ASSIGNED);

        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);

        // Send notification to the provider
        String message = String.format("You have been assigned to the service request: %s by %s %s",
                serviceRequest.getTitle(), client.getFirstName(), client.getLastName());
        notificationService.createNotification(provider, message);

        return convertToResponse(updatedRequest);
    }

    public ServiceRequestResponse updateBudget(Long requestId, Double budget, Long clientId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can update the budget");
        }

        if (!serviceRequest.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("Client does not own this request");
        }

        if (budget == null || budget <= 0) {
            throw new IllegalArgumentException("Budget must be a positive value");
        }

        if (serviceRequest.getStatus() == ServiceRequest.Status.COMPLETED ||
                serviceRequest.getStatus() == ServiceRequest.Status.CANCELLED ||
                serviceRequest.getStatus() == ServiceRequest.Status.DELETED) {
            throw new IllegalStateException("Cannot update budget for a request in " + serviceRequest.getStatus() + " status");
        }

        serviceRequest.setBudget(budget);
        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);

        // Send notification to the assigned provider, if any
        if (serviceRequest.getAssignedProvider() != null) {
            String message = String.format("The budget for the service request: %s has been updated to $%.2f by %s %s",
                    serviceRequest.getTitle(), budget, client.getFirstName(), client.getLastName());
            notificationService.createNotification(serviceRequest.getAssignedProvider(), message);
        }

        return convertToResponse(updatedRequest);
    }


    public ServiceRequestResponse updateRequestStatus(Long requestId, String status, Long userId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate user permissions
        if (user.getRole().equals(User.Role.CLIENT)) {
            if (!serviceRequest.getClient().getId().equals(userId)) {
                throw new UnauthorizedException("Client does not own this request");
            }
        } else if (user.getRole().equals(User.Role.PROVIDER)) {
            if (serviceRequest.getAssignedProvider() == null || !serviceRequest.getAssignedProvider().getId().equals(userId)) {
                throw new UnauthorizedException("Provider is not assigned to this request");
            }
        } else {
            throw new UnauthorizedException("User does not have permission to update request status");
        }

        // Normalize status input
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        // Remove all non-alphabetic characters and convert to uppercase
        String normalizedStatus = status.replaceAll("[^a-zA-Z]", "").toUpperCase();
        System.out.println("Raw status: [" + status + "], Normalized status: [" + normalizedStatus + "]");

        // Enforce state transitions
        String currentStatus = serviceRequest.getStatus().name();
        if (currentStatus.equals("OPEN")) {
            if (!normalizedStatus.equals("ASSIGNED") && !normalizedStatus.equals("CANCELLED")) {
                throw new IllegalStateException("Invalid transition from OPEN to " + normalizedStatus + " (original: " + status + ")");
            }
        } else if (currentStatus.equals("ASSIGNED")) {
            if (!normalizedStatus.equals("COMPLETED") && !normalizedStatus.equals("CANCELLED")) {
                throw new IllegalStateException("Invalid transition from ASSIGNED to " + normalizedStatus + " (original: " + status + ")");
            }
            if (normalizedStatus.equals("COMPLETED") && !user.getRole().equals(User.Role.PROVIDER)) {
                throw new UnauthorizedException("Only providers can mark a request as COMPLETED");
            }
        } else if (currentStatus.equals("COMPLETED") || currentStatus.equals("CANCELLED") || currentStatus.equals("DELETED")) {
            throw new IllegalStateException("Cannot change status from " + currentStatus + " to " + normalizedStatus + " (original: " + status + ")");
        }

        // Set status
        try {
            serviceRequest.setStatus(ServiceRequest.Status.valueOf(normalizedStatus));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be one of: OPEN, ASSIGNED, COMPLETED, CANCELLED, DELETED");
        }

        ServiceRequest updatedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToResponse(updatedRequest);
    }

    public void deleteRequest(Long requestId, Long userId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole().equals(User.Role.CLIENT)) {
            if (!serviceRequest.getClient().getId().equals(userId)) {
                throw new UnauthorizedException("Client does not own this request");
            }
            serviceRequest.setStatus(ServiceRequest.Status.DELETED);
            serviceRequestRepository.save(serviceRequest);
        } else if (user.getRole().equals(User.Role.ADMIN)) {
            serviceRequestRepository.delete(serviceRequest);
        } else {
            throw new UnauthorizedException("Not authorized to delete this request");
        }
    }

    private ServiceRequestResponse convertToResponse(ServiceRequest serviceRequest) {
        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setId(serviceRequest.getId());
        response.setClientId(serviceRequest.getClient().getId());
        response.setTitle(serviceRequest.getTitle());
        response.setEventName(serviceRequest.getEventName());
        response.setEventDate(serviceRequest.getEventDate());
        response.setLocation(serviceRequest.getLocation());
        response.setServiceType(serviceRequest.getServiceType());
        response.setDescription(serviceRequest.getDescription());
        response.setBudget(serviceRequest.getBudget());
        response.setStatus(serviceRequest.getStatus().name());
        response.setCreatedAt(serviceRequest.getCreatedAt());
        if (serviceRequest.getAssignedProvider() != null) {
            response.setAssignedProviderId(serviceRequest.getAssignedProvider().getId());
        }
        return response;
    }
}