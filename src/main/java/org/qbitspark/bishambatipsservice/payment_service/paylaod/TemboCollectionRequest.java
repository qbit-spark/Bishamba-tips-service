package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemboCollectionRequest {

    @JsonProperty("channel")
    private String channel; // TZ-VODACOM-C2B, TZ-TIGO-C2B, etc.

    @JsonProperty("msisdn")
    private String msisdn; // Phone number (255XXXXXXXXX)

    @JsonProperty("amount")
    private BigDecimal amount; // Payment amount

    @JsonProperty("transactionRef")
    private String transactionRef; // Unique transaction reference

    @JsonProperty("narration")
    private String narration; // Payment description

    @JsonProperty("transactionDate")
    private String transactionDate; // Format: yyyy-MM-dd HH:mm:ss

    @JsonProperty("callbackUrl")
    private String callbackUrl; // Webhook URL

    // Builder method for easy creation
    public static TemboCollectionRequest create(PaymentRequest paymentRequest, String channel, String callbackUrl) {
        return TemboCollectionRequest.builder()
                .channel(channel)
                .msisdn(paymentRequest.getCustomerPhone())
                .amount(paymentRequest.getAmount())
                .transactionRef(paymentRequest.getTransactionRef())
                .narration(paymentRequest.getDescription())
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .callbackUrl(callbackUrl)
                .build();
    }
}
