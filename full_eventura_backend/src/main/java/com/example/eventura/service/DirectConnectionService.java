package com.example.eventura.service;

import com.example.eventura.dto.request.DirectConnectionRequest;
import com.example.eventura.dto.response.DirectConnectionResponse;
import com.example.eventura.entity.DirectConnection;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.DirectConnectionRepository;
import com.example.eventura.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DirectConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(DirectConnectionService.class);

    @Autowired
    private DirectConnectionRepository directConnectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    public DirectConnectionResponse createDirectConnection(DirectConnectionRequest request, String email) {
        User client = userRepository.findByEmail(email);
        if (client == null || !client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can create connections");
        }

        User provider = userRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        DirectConnection connection = new DirectConnection();
        connection.setClient(client);
        connection.setProvider(provider);
        connection.setEventDetails(request.getEventDetails());
        connection.setProposedDate(request.getProposedDate());
        connection.setStatus(DirectConnection.ConnectionStatus.PENDING);

        DirectConnection savedConnection = directConnectionRepository.save(connection);

        // Notify the provider (both ways: notification and email)
        String message = String.format("New connection request from %s %s for %s on %s",
                client.getFirstName(), client.getLastName(), request.getEventDetails(), request.getProposedDate());
        notificationService.createNotification(provider, message);

        // Send connection request email
        try {
            emailService.sendConnectionRequestEmail(
                    provider.getEmail(),
                    "New Connection Request",
                    provider.getFirstName(),
                    provider.getLastName(),
                    client.getFirstName(),
                    client.getLastName(),
                    request.getEventDetails(),
                    request.getProposedDate()
            );
        } catch (MessagingException e) {
            logger.error("Failed to send connection request email to {}: {}", provider.getEmail(), e.getMessage());
            // Continue despite email failure
        }

        return convertToResponse(savedConnection);
    }

    public DirectConnectionResponse acceptConnection(Long connectionId, String email) {
        User provider = userRepository.findByEmail(email);
        if (provider == null || !provider.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can accept connections");
        }

        DirectConnection connection = directConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getProvider().getId().equals(provider.getId())) {
            throw new UnauthorizedException("Provider not associated with this connection");
        }

        if (connection.getStatus() != DirectConnection.ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is not in PENDING status");
        }

        connection.setStatus(DirectConnection.ConnectionStatus.ACCEPTED);
        DirectConnection updatedConnection = directConnectionRepository.save(connection);

        // Notify the client (both ways: notification and email)
        String message = String.format("Your connection request for %s has been accepted by %s %s",
                connection.getEventDetails(), provider.getFirstName(), provider.getLastName());
        notificationService.createNotification(connection.getClient(), message);

        // Send acceptance email
        try {
            emailService.sendConnectionAcceptanceEmail(
                    connection.getClient().getEmail(),
                    "Connection Request Accepted",
                    connection.getClient().getFirstName(),
                    connection.getClient().getLastName(),
                    connection.getEventDetails(),
                    provider.getFirstName(),
                    provider.getLastName()
            );
        } catch (MessagingException e) {
            logger.error("Failed to send acceptance email to {}: {}", connection.getClient().getEmail(), e.getMessage());
            // Continue despite email failure
        }

        return convertToResponse(updatedConnection);
    }

    public DirectConnectionResponse rejectConnection(Long connectionId, String email) {
        User provider = userRepository.findByEmail(email);
        if (provider == null || !provider.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can reject connections");
        }

        DirectConnection connection = directConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getProvider().getId().equals(provider.getId())) {
            throw new UnauthorizedException("Provider not associated with this connection");
        }

        if (connection.getStatus() != DirectConnection.ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is not in PENDING status");
        }

        connection.setStatus(DirectConnection.ConnectionStatus.REJECTED);
        DirectConnection updatedConnection = directConnectionRepository.save(connection);

        // Notify the client (both ways: notification and email)
        String message = String.format("Your connection request for %s has been rejected by %s %s",
                connection.getEventDetails(), provider.getFirstName(), provider.getLastName());
        notificationService.createNotification(connection.getClient(), message);

        // Send rejection email
        try {
            emailService.sendConnectionRejectionEmail(
                    connection.getClient().getEmail(),
                    "Connection Request Rejected",
                    connection.getClient().getFirstName(),
                    connection.getClient().getLastName(),
                    connection.getEventDetails(),
                    provider.getFirstName(),
                    provider.getLastName()
            );
        } catch (MessagingException e) {
            logger.error("Failed to send rejection email to {}: {}", connection.getClient().getEmail(), e.getMessage());
            // Continue despite email failure
        }

        return convertToResponse(updatedConnection);
    }

    public Page<DirectConnectionResponse> getConnectionsByClient(String email, Pageable pageable) {
        User client = userRepository.findByEmail(email);
        if (client == null) {
            throw new ResourceNotFoundException("Client not found");
        }

        return directConnectionRepository.findByClient(client, pageable)
                .map(this::convertToResponse);
    }

    public Page<DirectConnectionResponse> getConnectionsByProvider(String email, Pageable pageable) {
        User provider = userRepository.findByEmail(email);
        if (provider == null) {
            throw new ResourceNotFoundException("Provider not found");
        }

        return directConnectionRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }


    public DirectConnectionResponse getConnectionById(Long connectionId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        DirectConnection connection = directConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        // Check if the user is either the client or provider of the connection
        if (!connection.getClient().getId().equals(user.getId()) && !connection.getProvider().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to view this connection");
        }

        return convertToResponse(connection);
    }


    public void deleteDirectConnection(Long connectionId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can delete connections");
        }

        DirectConnection connection = directConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getClient().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to delete this connection");
        }

        directConnectionRepository.delete(connection);
    }

    private DirectConnectionResponse convertToResponse(DirectConnection connection) {
        DirectConnectionResponse response = new DirectConnectionResponse();
        response.setId(connection.getId());
        response.setClientId(connection.getClient().getId());
        response.setProviderId(connection.getProvider().getId());
        response.setEventDetails(connection.getEventDetails());
        response.setProposedDate(connection.getProposedDate());
        response.setStatus(connection.getStatus().name());
        response.setCreatedAt(connection.getCreatedAt());
        return response;
    }
}