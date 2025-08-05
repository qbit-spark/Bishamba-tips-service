package org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemboStatusResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String transactionRef;
    private String status;
    private String paymentStatus;
    private BigDecimal amount;
    private String currency;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime transactionDate;
    private Object data;

    public String getPaymentStatus() {
        // Fallback to status if paymentStatus is null
        return paymentStatus != null ? paymentStatus : status;
    }
}
