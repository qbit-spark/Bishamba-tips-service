package org.qbitspark.bishambatipsservice.payment_service.providers_services.impl.providers_helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.globe_api_client.HttpClientService;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentDirection;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.*;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod.TemboCollectionResponse;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod.TemboPayoutResponse;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod.TemboStatusResponse;
import org.qbitspark.bishambatipsservice.payment_service.utils.PaymentUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;


@Component
@Slf4j
@RequiredArgsConstructor
public class EnhancedTemboPaymentProvider {

    @Value("${payment.tembo.base-url}")
    private String baseUrl;

    @Value("${payment.tembo.account-id}")
    private String accountId;

    @Value("${payment.tembo.account-secret}")
    private String accountSecret;

    @Value("${payment.tembo.callback-url}")
    private String callbackUrl;

    @Value("${payment.tembo.bishamba-wallet}")
    private String disbursingWallet;

    private final HttpClientService httpClient;

    private final PaymentUtils paymentUtils;

    public PaymentResult processPayment(PaymentRequest request) {
        try {
            if (request.getDirection() == PaymentDirection.INBOUND) {
                return processInboundPayment(request);
            } else {
                return processOutboundPayment(request);
            }
        } catch (Exception e) {
            log.error("Tembo payment processing failed", e);
            return PaymentResult.failure("Payment processing failed: " + e.getMessage());
        }
    }

    // Process C2B Collection
    private PaymentResult processInboundPayment(PaymentRequest request) {
        String formattedPhone = paymentUtils.formatPhoneNumber(request.getCustomerPhone());
        String channel = paymentUtils.getTemboC2BChannel(formattedPhone);

        // Create a clean request DTO instead of Map
        TemboCollectionRequest temboRequest = TemboCollectionRequest.create(
                PaymentRequest.builder()
                        .customerPhone(formattedPhone)
                        .amount(request.getAmount())
                        .transactionRef(request.getTransactionRef())
                        .description(request.getDescription())
                        .build(),
                channel,
                callbackUrl
        );

        String url = baseUrl + "/collection";
        Map<String, String> headers = createHeaders();

        log.info("Sending USSD push request to Tembo for ref: {}, channel: {}",
                request.getTransactionRef(), channel);

        try {
            TemboCollectionResponse response = httpClient.post(url, temboRequest, headers, TemboCollectionResponse.class);

            if (response.isSuccess()) {
                return PaymentResult.success("USSD push sent successfully")
                        .withExternalTransactionId(response.getTransactionId())
                        .withStatus(mapTemboStatusToLocal(response.getStatus()))
                        .withAdditionalData("channel", channel)
                        .withAdditionalData("temboResponse", response);
            } else {
                throw new RuntimeException("Tembo API error: " + response.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Tembo API error", e);
        }
    }

    // Process B2C Payout
    private PaymentResult processOutboundPayment(PaymentRequest request) {
        String formattedPhone = paymentUtils.formatPhoneNumber(request.getCustomerPhone());
        String serviceCode = paymentUtils.getTemboB2CServiceCode(formattedPhone);

        // Create a clean request DTO instead of Map
        TemboPayoutRequest temboRequest = TemboPayoutRequest.create(
                PaymentRequest.builder()
                        .customerPhone(formattedPhone)
                        .customerName(request.getCustomerName())
                        .amount(request.getAmount())
                        .transactionRef(request.getTransactionRef())
                        .description(request.getDescription())
                        .build(),
                disbursingWallet,
                serviceCode,
                callbackUrl
        );

        String url = baseUrl + "/payment/wallet-to-mobile";
        Map<String, String> headers = createHeaders();

        log.info("Sending B2C payout to Tembo for ref: {}, serviceCode: {}",
                request.getTransactionRef(), serviceCode);

        try {
            TemboPayoutResponse response = httpClient.post(url, temboRequest, headers, TemboPayoutResponse.class);

            if (response.isSuccess()) {
                return PaymentResult.success("Payout initiated successfully")
                        .withExternalTransactionId(response.getTransactionId())
                        .withStatus(mapTemboStatusToLocal(response.getStatus()))
                        .withAdditionalData("serviceCode", serviceCode)
                        .withAdditionalData("temboResponse", response);
            } else {
                return PaymentResult.failure("Tembo payout error: " + response.getMessage());
            }

        } catch (Exception e) {
            log.error("Tembo payout failed", e);
            return PaymentResult.failure("Tembo payout failed: " + e.getMessage());
        }
    }


    public PaymentResult checkStatus(String transactionRef) {
        try {
            // Create a clean request DTO instead of Map
            TemboStatusRequest statusRequest = TemboStatusRequest.byTransactionRef(transactionRef);

            String url = baseUrl + "/collection/status";
            Map<String, String> headers = createHeaders();

            TemboStatusResponse response = httpClient.post(url, statusRequest, headers, TemboStatusResponse.class);

            if (response.isSuccess()) {
                return PaymentResult.success("Status retrieved")
                        .withStatus(mapTemboStatusToLocal(response.getPaymentStatus()))
                        .withAdditionalData("temboResponse", response);
            } else {
                return PaymentResult.failure("Status check error: " + response.getMessage());
            }

        } catch (Exception e) {
            log.error("Status check failed for ref: {}", transactionRef, e);
            return PaymentResult.failure("Status check error: " + e.getMessage());
        }
    }

    // Additional Tembo API methods
    public PaymentResult checkBalance() {
        try {
            TemboBalanceRequest balanceRequest = TemboBalanceRequest.create(accountId);

            String url = baseUrl + "/wallet/balance";
            Map<String, String> headers = createHeaders();

            TemboBalanceResponse response = httpClient.post(url, balanceRequest, headers, TemboBalanceResponse.class);

            if (response.isSuccess()) {
                return PaymentResult.success("Balance retrieved")
                        .withAdditionalData("balance", response.getBalance())
                        .withAdditionalData("currency", response.getCurrency());
            } else {
                return PaymentResult.failure("Balance check error: " + response.getMessage());
            }

        } catch (Exception e) {
            log.error("Balance check failed", e);
            return PaymentResult.failure("Balance check error: " + e.getMessage());
        }
    }

    public PaymentResult getStatement(LocalDate startDate, LocalDate endDate) {
        try {
            TemboStatementRequest statementRequest = TemboStatementRequest.create(accountId, startDate, endDate);

            String url = baseUrl + "/wallet/statement";
            Map<String, String> headers = createHeaders();

            TemboStatementResponse response = httpClient.post(url, statementRequest, headers, TemboStatementResponse.class);

            if (response.isSuccess()) {
                return PaymentResult.success("Statement retrieved")
                        .withAdditionalData("transactions", response.getTransactions())
                        .withAdditionalData("totalCount", response.getTotalCount());
            } else {
                return PaymentResult.failure("Statement error: " + response.getMessage());
            }

        } catch (Exception e) {
            log.error("Statement retrieval failed", e);
            return PaymentResult.failure("Statement error: " + e.getMessage());
        }
    }


    public boolean isAvailable() {
        return baseUrl != null && accountId != null && accountSecret != null;
    }

    // Helper methods
    private Map<String, String> createHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "x-account-id", accountId,
                "x-secret-key", accountSecret,
                "x-request-id", UUID.randomUUID().toString()
        );
    }

    private String mapTemboStatusToLocal(String temboStatus) {
        if (temboStatus == null) return "PENDING";

        return switch (temboStatus.toUpperCase()) {
            case "SUCCESS", "SUCCESSFUL", "COMPLETED" -> "COMPLETED";
            case "FAILED", "FAILURE" -> "FAILED";
            case "PENDING", "PROCESSING" -> "PROCESSING";
            default -> "PENDING";
        };
    }

}
