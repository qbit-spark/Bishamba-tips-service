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
public class TemboPayoutRequest {

    @JsonProperty("countryCode")
    private String countryCode; // TZ

    @JsonProperty("accountNo")
    private String accountNo; // Your Tembo account number

    @JsonProperty("serviceCode")
    private String serviceCode; // TZ-VODACOM-B2C, TZ-TIGO-B2C, etc.

    @JsonProperty("amount")
    private BigDecimal amount; // Payout amount

    @JsonProperty("msisdn")
    private String msisdn; // Recipient phone number

    @JsonProperty("narration")
    private String narration; // Payout description

    @JsonProperty("currencyCode")
    private String currencyCode; // TZS

    @JsonProperty("recipientNames")
    private String recipientNames; // Recipient full name

    @JsonProperty("transactionRef")
    private String transactionRef; // Unique transaction reference

    @JsonProperty("transactionDate")
    private String transactionDate; // Format: yyyy-MM-dd HH:mm:ss

    @JsonProperty("callbackUrl")
    private String callbackUrl; // Webhook URL

    // Builder method for easy creation
    public static TemboPayoutRequest create(PaymentRequest paymentRequest, String accountNo,
                                            String serviceCode, String callbackUrl) {
        return TemboPayoutRequest.builder()
                .countryCode("TZ")
                .accountNo(accountNo)
                .serviceCode(serviceCode)
                .amount(paymentRequest.getAmount())
                .msisdn(paymentRequest.getCustomerPhone())
                .narration(paymentRequest.getDescription())
                .currencyCode("TZS")
                .recipientNames(paymentRequest.getCustomerName())
                .transactionRef(paymentRequest.getTransactionRef())
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .callbackUrl(callbackUrl)
                .build();
    }
}
