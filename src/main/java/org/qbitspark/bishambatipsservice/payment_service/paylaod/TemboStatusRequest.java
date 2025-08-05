package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemboStatusRequest {

    @JsonProperty("transactionRef")
    private String transactionRef; // Transaction reference to check

    @JsonProperty("transactionId")
    private String transactionId; // Alternative: Tembo transaction ID

    // Static factory methods
    public static TemboStatusRequest byTransactionRef(String transactionRef) {
        return TemboStatusRequest.builder()
                .transactionRef(transactionRef)
                .build();
    }

    public static TemboStatusRequest byTransactionId(String transactionId) {
        return TemboStatusRequest.builder()
                .transactionId(transactionId)
                .build();
    }

    public static TemboStatusRequest byBoth(String transactionRef, String transactionId) {
        return TemboStatusRequest.builder()
                .transactionRef(transactionRef)
                .transactionId(transactionId)
                .build();
    }
}

