package com.example.eventura.controller;

import com.example.eventura.dto.request.PaymentRequest;
import com.example.eventura.dto.request.PaymentStatusRequest;
import com.example.eventura.dto.response.PaymentResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        PaymentResponse response = paymentService.createPayment(request, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByClient(
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        Page<PaymentResponse> payments = paymentService.getPaymentsByClient(email, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByProvider(
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        Page<PaymentResponse> payments = paymentService.getPaymentsByProvider(email, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable Long paymentId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        PaymentResponse response = paymentService.getPaymentStatus(paymentId, email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody PaymentStatusRequest statusRequest,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, email, statusRequest.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/request/{requestId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public ResponseEntity<PaymentResponse> getPaymentStatusByRequestId(
            @PathVariable Long requestId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        PaymentResponse response = paymentService.getPaymentStatusByRequestId(requestId, email);
        return ResponseEntity.ok(response);
    }
}