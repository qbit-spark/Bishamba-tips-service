package org.qbitspark.bishambatipsservice.payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.globeresponsebody.GlobeSuccessResponseBuilder;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentDirection;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentMethod;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentType;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResponse;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.InitiatePaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.service.PaymentService;
import org.qbitspark.bishambatipsservice.payment_service.utils.PaymentUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentUtils paymentUtils;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {

        // Build payment request
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .transactionRef(paymentUtils.generateTransactionRef(request.getPaymentType()))
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .paymentType(request.getPaymentType())
                .paymentMethod(request.getPaymentMethod())
                .direction(PaymentDirection.INBOUND) // Default inbound
                .amount(request.getAmount())
                .currencyCode("TZS")
                .description(request.getDescription() != null ? request.getDescription() : "Payment")
                .build();

        PaymentResult result = paymentService.initiatePayment(paymentRequest);

        if (result.isSuccess()) {
            return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                    "Payment initiated successfully", result));
        } else {
            return ResponseEntity.badRequest().body(GlobeSuccessResponseBuilder.success(
                    result.getMessage(), result));
        }
    }

    @PostMapping("/callback/{transactionRef}")
    public ResponseEntity<String> handleCallback(
            @PathVariable String transactionRef,
            @RequestBody Map<String, Object> callbackData) {

        log.info("Received callback for transaction: {}", transactionRef);

        PaymentResult result = paymentService.processCallback(transactionRef, callbackData);

        if (result.isSuccess()) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body("FAILED");
        }
    }

    @GetMapping("/{transactionRef}")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getPayment(
            @PathVariable String transactionRef) {

        PaymentEntity payment = paymentService.getPaymentByTransactionRef(transactionRef);

        if (payment != null) {
            PaymentResponse response = PaymentResponse.fromEntity(payment);
            return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                    "Payment retrieved", response));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getCustomerPayments(
            @PathVariable UUID customerId) {

        List<PaymentEntity> payments = paymentService.getPaymentsByCustomer(customerId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Payments retrieved", responses));
    }

    @PostMapping("/{transactionRef}/retry")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> retryPayment(
            @PathVariable String transactionRef) {

        PaymentResult result = paymentService.retryFailedPayment(transactionRef);

        if (result.isSuccess()) {
            return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                    "Payment retry initiated", result));
        } else {
            return ResponseEntity.badRequest().body(GlobeSuccessResponseBuilder.success(
                    result.getMessage(), result));
        }
    }

    @GetMapping("/{transactionRef}/status")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> checkStatus(
            @PathVariable String transactionRef) {

        PaymentResult result = paymentService.checkPaymentStatus(transactionRef);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Status checked", result));
    }
}