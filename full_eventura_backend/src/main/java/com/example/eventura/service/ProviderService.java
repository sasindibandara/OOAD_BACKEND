package com.example.eventura.service;

import com.example.eventura.dto.request.PortfolioRequest;
import com.example.eventura.dto.request.ProviderProfileRequest;
import com.example.eventura.dto.request.VerificationDocumentRequest;
import com.example.eventura.dto.response.PortfolioResponse;
import com.example.eventura.dto.response.ProviderResponse;
import com.example.eventura.dto.response.VerificationDocumentResponse;
import com.example.eventura.entity.Portfolio;
import com.example.eventura.entity.ServiceProvider;
import com.example.eventura.entity.User;
import com.example.eventura.entity.VerificationDocument;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.PortfolioRepository;
import com.example.eventura.repository.ServiceProviderRepository;
import com.example.eventura.repository.UserRepository;
import com.example.eventura.repository.VerificationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final VerificationDocumentRepository verificationDocumentRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificationService notificationService;

    public ProviderResponse createProviderProfile(Long userId, ProviderProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can create profiles");
        }

        ServiceProvider existingProvider = serviceProviderRepository.findByUser(user);
        if (existingProvider != null) {
            throw new ResourceNotFoundException("Provider profile already exists");
        }

        ServiceProvider provider = new ServiceProvider();
        provider.setUser(user);
        provider.setCompanyName(request.getCompanyName());
        provider.setServiceType(request.getServiceType());
        provider.setAddress(request.getAddress());
        provider.setMobileNumber(request.getMobileNumber());
        provider.setIsVerified(false);

        ServiceProvider savedProvider = serviceProviderRepository.save(provider);

        return convertToResponse(savedProvider);
    }

    public ProviderResponse updateProviderProfile(Long userId, ProviderProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can update profiles");
        }

        ServiceProvider provider = serviceProviderRepository.findByUser(user);
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found");
        }

        // Update fields if provided
        if (request.getCompanyName() != null) {
            provider.setCompanyName(request.getCompanyName());
        }
        if (request.getServiceType() != null) {
            provider.setServiceType(request.getServiceType());
        }
        if (request.getAddress() != null) {
            provider.setAddress(request.getAddress());
        }
        if (request.getMobileNumber() != null) {
            provider.setMobileNumber(request.getMobileNumber());
        }

        ServiceProvider updatedProvider = serviceProviderRepository.save(provider);

        // Send normal notification
        String notificationMessage = String.format("Your provider profile for %s has been updated", provider.getCompanyName());
        notificationService.createNotification(user, notificationMessage);

        return convertToResponse(updatedProvider);
    }

    public ProviderResponse getOwnProviderProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can access their own profile");
        }

        ServiceProvider provider = serviceProviderRepository.findByUser(user);
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found");
        }

        return convertToResponse(provider);
    }

    public Page<ProviderResponse> getAllProviders(Pageable pageable) {
        return serviceProviderRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    public ProviderResponse getProviderProfile(Long providerId) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        return convertToResponse(provider);
    }

    public Long getProviderIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("User is not a provider");
        }

        ServiceProvider provider = serviceProviderRepository.findByUser(user);
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found for user");
        }

        return provider.getId();
    }

    public VerificationDocumentResponse uploadDocument(Long providerId, VerificationDocumentRequest request) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        VerificationDocument document = new VerificationDocument();
        document.setProvider(provider);
        document.setDocumentType(request.getDocumentType());
        document.setDocumentUrl(request.getDocumentUrl());
        document.setStatus(VerificationDocument.Status.PENDING);

        VerificationDocument savedDocument = verificationDocumentRepository.save(document);

        return convertToDocumentResponse(savedDocument);
    }

    public VerificationDocumentResponse updateDocumentStatus(Long documentId, String status) {
        VerificationDocument document = verificationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        try {
            document.setStatus(VerificationDocument.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid status: " + status);
        }

        // If all documents are approved, mark provider as verified
        if (document.getStatus() == VerificationDocument.Status.APPROVED) {
            checkAndUpdateProviderVerification(document.getProvider());
        }

        VerificationDocument updatedDocument = verificationDocumentRepository.save(document);
        return convertToDocumentResponse(updatedDocument);
    }

    public ProviderResponse updateProviderVerificationStatus(Long providerId, Boolean isVerified) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        provider.setIsVerified(isVerified);
        ServiceProvider updatedProvider = serviceProviderRepository.save(provider);

        // Send notification
        String notificationMessage = String.format("Your provider verification status has been %s",
                isVerified ? "verified" : "unverified");
        notificationService.createNotification(provider.getUser(), notificationMessage);

        return convertToResponse(updatedProvider);
    }

    public PortfolioResponse createPortfolio(Long providerId, PortfolioRequest request) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setProvider(provider);
        portfolio.setTitle(request.getTitle());
        portfolio.setDescription(request.getDescription());
        portfolio.setImageUrl(request.getImageUrl());
        portfolio.setProjectDate(request.getProjectDate());
        portfolio.setEventType(request.getEventType());
        portfolio.setStatus(Portfolio.Status.ACTIVE);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return convertToPortfolioResponse(savedPortfolio);
    }

    public Page<PortfolioResponse> getPortfolios(Long providerId, Pageable pageable) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        return portfolioRepository.findByProvider(provider, pageable)
                .map(this::convertToPortfolioResponse);
    }

    public void deletePortfolio(Long providerId, Long portfolioId, Long userId) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getRole().equals(User.Role.PROVIDER)) {
            throw new UnauthorizedException("Only providers can delete portfolios");
        }

        if (!provider.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Provider does not own this portfolio");
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        if (!portfolio.getProvider().getId().equals(providerId)) {
            throw new UnauthorizedException("Portfolio does not belong to this provider");
        }

        portfolioRepository.delete(portfolio);

        // Send notification
        String notificationMessage = String.format("Portfolio '%s' has been deleted", portfolio.getTitle());
        notificationService.createNotification(user, notificationMessage);
    }

    private void checkAndUpdateProviderVerification(ServiceProvider provider) {
        boolean allApproved = verificationDocumentRepository.findByProvider(provider, Pageable.unpaged())
                .stream()
                .allMatch(doc -> doc.getStatus() == VerificationDocument.Status.APPROVED);

        if (allApproved && !provider.getIsVerified()) {
            provider.setIsVerified(true);
            serviceProviderRepository.save(provider);
        }
    }

    private ProviderResponse convertToResponse(ServiceProvider provider) {
        ProviderResponse response = new ProviderResponse();
        response.setId(provider.getId());
        response.setUserId(provider.getUser().getId());
        response.setCompanyName(provider.getCompanyName());
        response.setServiceType(provider.getServiceType());
        response.setAddress(provider.getAddress());
        response.setMobileNumber(provider.getMobileNumber());
        response.setIsVerified(provider.getIsVerified());
        return response;
    }

    private VerificationDocumentResponse convertToDocumentResponse(VerificationDocument document) {
        VerificationDocumentResponse response = new VerificationDocumentResponse();
        response.setId(document.getId());
        response.setProviderId(document.getProvider().getId());
        response.setDocumentType(document.getDocumentType());
        response.setDocumentUrl(document.getDocumentUrl());
        response.setStatus(document.getStatus().name());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }

    private PortfolioResponse convertToPortfolioResponse(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.setId(portfolio.getId());
        response.setProviderId(portfolio.getProvider().getId());
        response.setTitle(portfolio.getTitle());
        response.setDescription(portfolio.getDescription());
        response.setImageUrl(portfolio.getImageUrl());
        response.setProjectDate(portfolio.getProjectDate());
        response.setEventType(portfolio.getEventType());
        response.setStatus(portfolio.getStatus().name());
        response.setCreatedAt(portfolio.getCreatedAt());
        return response;
    }
}