package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentDirection;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentMethod;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private String transactionRef;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private PaymentDirection direction;
    private BigDecimal amount;
    private String currencyCode;
    private String description;
    private UUID recordedByAgentId;
}