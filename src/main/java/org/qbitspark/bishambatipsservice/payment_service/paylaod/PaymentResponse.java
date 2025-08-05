package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private String transactionRef;
    private String externalTransactionId;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String paymentType;
    private String paymentMethod;
    private String direction;
    private BigDecimal amount;
    private String currencyCode;
    private String status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Boolean receiptGenerated;

    public static PaymentResponse fromEntity(PaymentEntity payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .externalTransactionId(payment.getExternalTransactionId())
                .customerId(payment.getCustomerId())
                .customerName(payment.getCustomerName())
                .customerPhone(maskPhoneNumber(payment.getCustomerPhone()))
                .paymentType(payment.getPaymentType().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .direction(payment.getDirection().name())
                .amount(payment.getAmount())
                .currencyCode(payment.getCurrencyCode())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .receiptGenerated(payment.getReceiptGenerated())
                .build();
    }

    private static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) return "***";
        return phone.substring(0, 6) + "****" + phone.substring(phone.length() - 2);
    }
}