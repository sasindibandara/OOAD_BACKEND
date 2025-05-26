package com.example.eventura.controller;

import com.example.eventura.dto.request.PortfolioRequest;
import com.example.eventura.dto.request.ProviderProfileRequest;
import com.example.eventura.dto.request.VerificationDocumentRequest;
import com.example.eventura.dto.response.ProviderResponse;
import com.example.eventura.dto.response.VerificationDocumentResponse;
import com.example.eventura.dto.response.PortfolioResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.ProviderService;
import com.example.eventura.service.VerificationDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final VerificationDocumentService verificationDocumentService;
    private final JwtTokenProvider jwtTokenProvider;

    //Set Up Provider Profile
    @PostMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> createProviderProfile(@RequestBody ProviderProfileRequest request,
                                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return new ResponseEntity<>(providerService.createProviderProfile(userId, request), HttpStatus.CREATED);
    }

    //Update Provider Profile
    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> updateProviderProfile(@RequestBody ProviderProfileRequest request,
                                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(providerService.updateProviderProfile(userId, request));
    }

    // Get Own Provider Profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> getOwnProviderProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(providerService.getOwnProviderProfile(userId));
    }

    //Get All Providers
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<Page<ProviderResponse>> getAllProviders(Pageable pageable) {
        return ResponseEntity.ok(providerService.getAllProviders(pageable));
    }

    // Get Provider by ID (Accessible to CLIENT, PROVIDER, and ADMIN roles)
    @GetMapping("/{providerId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderResponse> getProviderProfile(@PathVariable Long providerId) {
        return ResponseEntity.ok(providerService.getProviderProfile(providerId));
    }

    //Get Provider ID by User ID
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'PROVIDER')")
    public ResponseEntity<Long> getProviderIdByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(providerService.getProviderIdByUserId(userId));
    }

    //Provider Post Documents To Verified
    @PostMapping("/{providerId}/documents")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<VerificationDocumentResponse> uploadDocument(@PathVariable Long providerId,
                                                                       @RequestBody VerificationDocumentRequest request) {
        return new ResponseEntity<>(verificationDocumentService.uploadDocument(providerId, request), HttpStatus.CREATED);
    }

    //Get Documents By Provider ID
    @GetMapping("/{providerId}/documents")
    public ResponseEntity<Page<VerificationDocumentResponse>> getDocuments(@PathVariable Long providerId, Pageable pageable) {
        return ResponseEntity.ok(verificationDocumentService.getDocuments(providerId, pageable));
    }

    //Post Provider Portfolio
    @PostMapping("/{providerId}/portfolios")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PortfolioResponse> createPortfolio(@PathVariable Long providerId,
                                                             @RequestBody PortfolioRequest request) {
        return new ResponseEntity<>(providerService.createPortfolio(providerId, request), HttpStatus.CREATED);
    }

    //Get Portfolio By Provider ID
    @GetMapping("/{providerId}/portfolios")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<Page<PortfolioResponse>> getPortfolios(@PathVariable Long providerId, Pageable pageable) {
        return ResponseEntity.ok(providerService.getPortfolios(providerId, pageable));
    }

    //Delete Portfolio By Portfolio ID
    @DeleteMapping("/{providerId}/portfolios/{portfolioId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long providerId,
                                                @PathVariable Long portfolioId,
                                                @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        providerService.deletePortfolio(providerId, portfolioId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
}