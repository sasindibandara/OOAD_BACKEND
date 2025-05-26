package com.example.eventura.controller;

import com.example.eventura.dto.request.ServiceRequestRequest;
import com.example.eventura.dto.response.ServiceRequestResponse;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final JwtTokenProvider jwtTokenProvider;

    //post a Request as A client
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ServiceRequestResponse> createRequest(
            @RequestBody ServiceRequestRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long clientId = getUserIdFromToken(authHeader);
        return new ResponseEntity<>(requestService.createRequest(clientId, request), HttpStatus.CREATED);
    }

    //Get Request By Request ID
    @GetMapping("/{requestId}")
    public ResponseEntity<ServiceRequestResponse> getRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.getRequest(requestId));
    }

    //Get All Request to Show Providers
    @GetMapping
    public ResponseEntity<Page<ServiceRequestResponse>> getAllRequests(
            Pageable pageable,
            @RequestParam(required = false) String serviceType) {
        return ResponseEntity.ok(requestService.getAllRequests(pageable, serviceType));
    }

    //Get Own Request Posted By CLient
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ServiceRequestResponse>> getMyRequests(
            Pageable pageable,
            @RequestHeader("Authorization") String authHeader) {
        Long clientId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(requestService.getClientRequests(clientId, pageable));
    }


    //Assign Provider to own request
    @PutMapping("/{requestId}/assign/{providerId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ServiceRequestResponse> assignProvider(
            @PathVariable Long requestId,
            @PathVariable Long providerId,
            @RequestHeader("Authorization") String authHeader) {
        Long clientId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(requestService.assignProvider(requestId, providerId, clientId));
    }

    //Update Budget of a Request
    @PutMapping("/{requestId}/budget")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ServiceRequestResponse> updateBudget(
            @PathVariable Long requestId,
            @RequestBody Double budget,
            @RequestHeader("Authorization") String authHeader) {
        Long clientId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(requestService.updateBudget(requestId, budget, clientId));
    }


    //Set Request Status OPEN, ASSIGNED, COMPLETED, CANCELLED, DELETED
    @PutMapping("/{requestId}/status")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<ServiceRequestResponse> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestBody String status,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(requestService.updateRequestStatus(requestId, status, userId));
    }

   //Delete Reuest By Request ID (Only allow Client Who Owns request And Admin)
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long requestId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        requestService.deleteRequest(requestId, userId);
        return ResponseEntity.noContent().build();
    }


private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
}