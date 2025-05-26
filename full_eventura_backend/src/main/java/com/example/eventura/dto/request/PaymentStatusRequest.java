package com.example.eventura.dto.request;

import com.example.eventura.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusRequest {
    private Payment.PaymentStatus status;
}