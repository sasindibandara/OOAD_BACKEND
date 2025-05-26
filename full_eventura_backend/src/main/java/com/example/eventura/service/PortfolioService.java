package com.example.eventura.service;

import com.example.eventura.dto.request.PortfolioRequest;
import com.example.eventura.dto.response.PortfolioResponse;
import com.example.eventura.entity.Portfolio;
import com.example.eventura.entity.ServiceProvider;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.repository.PortfolioRepository;
import com.example.eventura.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ServiceProviderRepository serviceProviderRepository;

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
        return convertToResponse(savedPortfolio);
    }

    public Page<PortfolioResponse> getPortfolios(Long providerId, Pageable pageable) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        return portfolioRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }

    public Page<PortfolioResponse> getAllPortfolios(String status, Pageable pageable) {
        Page<Portfolio> portfolios;

        if (status != null && !status.isEmpty()) {
            try {
                Portfolio.Status portfolioStatus = Portfolio.Status.valueOf(status.toUpperCase());

                // Create specification for status filtering
                Specification<Portfolio> spec = (root, query, cb) ->
                        cb.equal(root.get("status"), portfolioStatus);

                portfolios = portfolioRepository.findAll(spec, pageable);
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Invalid status: " + status);
            }
        } else {
            portfolios = portfolioRepository.findAll(pageable);
        }

        return portfolios.map(this::convertToResponse);
    }

    public PortfolioResponse updatePortfolioStatus(Long portfolioId, String status) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        try {
            portfolio.setStatus(Portfolio.Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid status: " + status);
        }

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return convertToResponse(updatedPortfolio);
    }

    private PortfolioResponse convertToResponse(Portfolio portfolio) {
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