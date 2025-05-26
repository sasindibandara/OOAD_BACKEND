package com.example.eventura.service;

import com.example.eventura.dto.request.PaymentRequest;
import com.example.eventura.dto.request.PaymentStatusRequest;
import com.example.eventura.dto.response.PaymentResponse;
import com.example.eventura.entity.Payment;
import com.example.eventura.entity.ServiceRequest;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.PaymentRepository;
import com.example.eventura.repository.ServiceRequestRepository;
import com.example.eventura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public PaymentResponse createPayment(PaymentRequest request, String email) {
        User client = userRepository.findByEmail(email);
        if (client == null || !client.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedException("Only clients can create payments");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        if (!serviceRequest.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Not authorized to create payment for this request");
        }

        if (serviceRequest.getAssignedProvider() == null) {
            throw new ResourceNotFoundException("No provider assigned to this request");
        }

        Payment payment = new Payment();
        payment.setRequest(serviceRequest);
        payment.setClient(client);
        payment.setProvider(serviceRequest.getAssignedProvider());
        payment.setAmount(request.getAmount());
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        return convertToResponse(savedPayment);
    }

    public Page<PaymentResponse> getPaymentsByClient(String email, Pageable pageable) {
        User client = userRepository.findByEmail(email);
        if (client == null) {
            throw new ResourceNotFoundException("Client not found");
        }

        return paymentRepository.findByClient(client, pageable)
                .map(this::convertToResponse);
    }

    public Page<PaymentResponse> getPaymentsByProvider(String email, Pageable pageable) {
        User provider = userRepository.findByEmail(email);
        if (provider == null) {
            throw new ResourceNotFoundException("Provider not found");
        }

        return paymentRepository.findByProvider(provider, pageable)
                .map(this::convertToResponse);
    }

    public PaymentResponse getPaymentStatus(Long paymentId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getClient().getId().equals(user.getId()) && !payment.getProvider().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to view this payment status");
        }

        return convertToResponse(payment);
    }

    public PaymentResponse updatePaymentStatus(Long paymentId, String email, Payment.PaymentStatus status) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        System.out.println("payment updated status  huu");
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getClient().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the client can update payment status");
        }

        payment.setPaymentStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);

        if (status == Payment.PaymentStatus.COMPLETED) {
            User provider = payment.getProvider();
            User client = payment.getClient();
            ServiceRequest request = payment.getRequest();

            System.out.println("completee meessssaaggeeee");
            // Notify provider
            String providerMessage = String.format("You received a payment of Rs %s for service request: %s from %s %s",
                    payment.getAmount(), request.getTitle(), client.getFirstName(), client.getLastName());
            notificationService.createNotification(provider, providerMessage);



            // Notify client
            String clientMessage = String.format("Your payment of Rs %s for service request: %s was successful",
                    payment.getAmount(), request.getTitle());
            notificationService.createNotification(client, clientMessage);

        }

        return convertToResponse(updatedPayment);
    }

    public PaymentResponse getPaymentStatusByRequestId(Long requestId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        if (!serviceRequest.getClient().getId().equals(user.getId()) &&
                (serviceRequest.getAssignedProvider() == null ||
                        !serviceRequest.getAssignedProvider().getId().equals(user.getId()))) {
            throw new UnauthorizedException("Not authorized to view payment status for this request");
        }

        Payment payment = paymentRepository.findTopByRequestOrderByCreatedAtDesc(serviceRequest)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this service request"));

        return convertToResponse(payment);
    }

    private PaymentResponse convertToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setRequestId(payment.getRequest().getId());
        response.setClientId(payment.getClient().getId());
        response.setProviderId(payment.getProvider().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentStatus(payment.getPaymentStatus().name());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}