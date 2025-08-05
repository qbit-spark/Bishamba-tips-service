package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String message;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String externalTransactionId;
    private Map<String, Object> additionalData;

    public static PaymentResult success(String message) {
        return PaymentResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static PaymentResult failure(String message) {
        return PaymentResult.builder()
                .success(false)
                .message(message)
                .status("FAILED")
                .build();
    }

    public PaymentResult withStatus(String status) {
        this.status = status;
        return this;
    }

    public PaymentResult withExternalTransactionId(String externalId) {
        this.externalTransactionId = externalId;
        return this;
    }

    public PaymentResult withAdditionalData(String key, Object value) {
        if (this.additionalData == null) {
            this.additionalData = new HashMap<>();
        }
        this.additionalData.put(key, value);
        return this;
    }
}

