package org.qbitspark.bishambatipsservice.payment_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.*;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.PaymentProviderService;
import org.qbitspark.bishambatipsservice.payment_service.repo.PaymentRepository;
import org.qbitspark.bishambatipsservice.payment_service.service.PaymentService;
import org.qbitspark.bishambatipsservice.payment_service.service.CommissionService;
import org.qbitspark.bishambatipsservice.payment_service.utils.PaymentUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderService providerService;
    private final PaymentUtils paymentUtils;
    private final CommissionService commissionService;

    @Override
    @Transactional
    public PaymentResult initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for ref: {}", request.getTransactionRef());

        // Validate request
        if (!isValidPaymentRequest(request)) {
            return PaymentResult.failure("Invalid payment request");
        }

        // Check if transaction already exists
        if (paymentRepository.existsByTransactionRef(request.getTransactionRef())) {
            return PaymentResult.failure("Transaction reference already exists");
        }

        // Create payment entity
        PaymentEntity payment = createPaymentEntity(request);
        payment = paymentRepository.save(payment);

        // Determine payment provider
        PaymentProvider provider = determineProvider(request);

        // Process payment with provider
        PaymentResult providerResult = providerService.processPaymentViaProvider(request, provider);

        // Update payment based on provider result
        updatePaymentFromResult(payment, providerResult, provider);
        paymentRepository.save(payment);

        log.info("Payment initiated: ref={}, status={}",
                request.getTransactionRef(), payment.getStatus());

        return providerResult;
    }

    @Override
    @Transactional
    public PaymentResult processCallback(String transactionRef, Map<String, Object> callbackData) {
        log.info("Processing callback for ref: {}", transactionRef);

        PaymentEntity payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElse(null);

        if (payment == null) {
            log.warn("Payment not found for callback: {}", transactionRef);
            return PaymentResult.failure("Payment not found");
        }

        // Extract status from callback
        String callbackStatus = extractCallbackStatus(callbackData);

        // Update payment status
        updatePaymentFromCallback(payment, callbackStatus, callbackData);
        paymentRepository.save(payment);

        // If payment completed, trigger commission calculation
        if (payment.isCompleted()) {
            try {
                commissionService.calculateCommission(payment);
                log.info("Commission calculated for payment: {}", transactionRef);
            } catch (Exception e) {
                log.error("Failed to calculate commission for payment: {}", transactionRef, e);
            }
        }

        return PaymentResult.success("Callback processed")
                .withStatus(payment.getStatus().name());
    }

    @Override
    public PaymentEntity getPaymentByTransactionRef(String transactionRef) {
        return paymentRepository.findByTransactionRef(transactionRef)
                .orElse(null);
    }

    @Override
    public List<PaymentEntity> getPaymentsByCustomer(UUID customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public PaymentResult retryFailedPayment(String transactionRef) {
        PaymentEntity payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElse(null);

        if (payment == null) {
            return PaymentResult.failure("Payment not found");
        }

        if (!payment.canRetry()) {
            return PaymentResult.failure("Payment cannot be retried");
        }

        // Create new payment request from existing payment
        PaymentRequest retryRequest = createRetryRequest(payment);

        // Process with provider
        PaymentProvider provider = payment.getPaymentProvider();
        PaymentResult result = providerService.processPaymentViaProvider(retryRequest, provider);

        // Update payment
        payment.incrementRetryCount();
        updatePaymentFromResult(payment, result, provider);
        paymentRepository.save(payment);

        return result;
    }

    @Override
    public PaymentResult checkPaymentStatus(String transactionRef) {
        PaymentEntity payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElse(null);

        if (payment == null) {
            return PaymentResult.failure("Payment not found");
        }

        // Check with provider if payment is still pending
        if (payment.isPending() || payment.isProcessing()) {
            PaymentProvider provider = payment.getPaymentProvider();
            PaymentResult statusResult = providerService.checkPaymentStatusViaProvider(transactionRef, provider);

            // Update payment if status changed
            if (statusResult.isSuccess() && !statusResult.getStatus().equals(payment.getStatus().name())) {
                updatePaymentStatus(payment, statusResult.getStatus());
                paymentRepository.save(payment);
            }

            return statusResult;
        }

        return PaymentResult.success("Status retrieved")
                .withStatus(payment.getStatus().name());
    }

    // Helper methods
    private boolean isValidPaymentRequest(PaymentRequest request) {
        return request.getTransactionRef() != null &&
                request.getAmount() != null &&
                request.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                request.getCustomerPhone() != null &&
                paymentUtils.isValidTanzanianPhoneNumber(request.getCustomerPhone());
    }

    private PaymentEntity createPaymentEntity(PaymentRequest request) {
        return PaymentEntity.builder()
                .transactionRef(request.getTransactionRef())
                .customerId(request.getCustomerId())
                .customerType(CustomerType.FARMER) // Default for now
                .customerName(request.getCustomerName())
                .customerPhone(paymentUtils.formatPhoneNumber(request.getCustomerPhone()))
                .paymentType(request.getPaymentType())
                .paymentMethod(request.getPaymentMethod())
                .direction(request.getDirection())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "TZS")
                .totalAmount(request.getAmount()) // Add fees later
                .status(PaymentStatus.PENDING)
                .recordedByAgentId(request.getRecordedByAgentId())
                .createdAt(LocalDateTime.now())
                .initiatedAt(LocalDateTime.now())
                .build();
    }

    private PaymentProvider determineProvider(PaymentRequest request) {
        // For now, default to TEMBO for mobile payments, CASH for cash
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            return PaymentProvider.CASH;
        }
        return PaymentProvider.TEMBO;
    }

    private void updatePaymentFromResult(PaymentEntity payment, PaymentResult result, PaymentProvider provider) {
        payment.setPaymentProvider(provider);

        if (result.isSuccess()) {
            payment.markAsProcessing();
            if (result.getExternalTransactionId() != null) {
                payment.setExternalTransactionId(result.getExternalTransactionId());
            }
        } else {
            payment.markAsFailed(result.getMessage());
        }
    }

    private String extractCallbackStatus(Map<String, Object> callbackData) {
        // Try different possible status fields
        Object status = callbackData.get("status");
        if (status == null) status = callbackData.get("paymentStatus");
        if (status == null) status = callbackData.get("transactionStatus");

        return status != null ? status.toString() : "UNKNOWN";
    }

    private void updatePaymentFromCallback(PaymentEntity payment, String callbackStatus, Map<String, Object> callbackData) {
        payment.processCallback(callbackData.toString());

        switch (callbackStatus.toUpperCase()) {
            case "SUCCESS", "SUCCESSFUL", "COMPLETED" -> {
                payment.markAsCompleted();
            }
            case "FAILED", "FAILURE" -> {
                String reason = (String) callbackData.get("failureReason");
                payment.markAsFailed(reason != null ? reason : "Payment failed");
            }
            case "PROCESSING", "PENDING" -> {
                payment.markAsProcessing();
            }
        }
    }

    private PaymentRequest createRetryRequest(PaymentEntity payment) {
        return PaymentRequest.builder()
                .transactionRef(payment.getTransactionRef())
                .customerId(payment.getCustomerId())
                .customerName(payment.getCustomerName())
                .customerPhone(payment.getCustomerPhone())
                .paymentType(payment.getPaymentType())
                .paymentMethod(payment.getPaymentMethod())
                .direction(payment.getDirection())
                .amount(payment.getAmount())
                .currencyCode(payment.getCurrencyCode())
                .recordedByAgentId(payment.getRecordedByAgentId())
                .build();
    }

    private void updatePaymentStatus(PaymentEntity payment, String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED" -> payment.markAsCompleted();
            case "FAILED" -> payment.markAsFailed("Status check revealed failure");
            case "PROCESSING" -> payment.markAsProcessing();
        }
    }
}