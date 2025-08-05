package org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemboCollectionResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String transactionRef;
    private String status;
    private String channel;
    private BigDecimal amount;
    private String currency;
    private String errorCode;
    private String errorMessage;
    private Object data;

    public boolean hasTransactionId() {
        return transactionId != null && !transactionId.isEmpty();
    }
}