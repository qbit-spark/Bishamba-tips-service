package org.qbitspark.bishambatipsservice.payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.globeresponsebody.GlobeSuccessResponseBuilder;
import org.qbitspark.bishambatipsservice.payment_service.entity.BillingEntity;
import org.qbitspark.bishambatipsservice.payment_service.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.CreateBillingRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.SuspendBillingRequest;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> createBilling(
            @Valid @RequestBody CreateBillingRequest request) {

        BillingEntity billing = billingService.createBilling(
                request.getCustomerId(),
                request.getServiceName(),
                request.getAmount());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Billing created successfully", billing));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ROLE_AGENT') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getCustomerBillings(
            @PathVariable UUID customerId) {

        List<BillingEntity> billings = billingService.getCustomerBillings(customerId);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Customer billings retrieved", billings));
    }

    @GetMapping("/due")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> getDueBillings() {

        List<BillingEntity> dueBillings = billingService.getDueBillings();

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Due billings retrieved", dueBillings));
    }

    @PostMapping("/{billingId}/suspend")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> suspendBilling(
            @PathVariable UUID billingId,
            @Valid @RequestBody SuspendBillingRequest request) {

        billingService.suspendBilling(billingId, request.getReason());

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Billing suspended", null));
    }

    @PostMapping("/{billingId}/reactivate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> reactivateBilling(
            @PathVariable UUID billingId) {

        billingService.reactivateBilling(billingId);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Billing reactivated", null));
    }

    @PostMapping("/{billingId}/process")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GlobeSuccessResponseBuilder> processBilling(
            @PathVariable UUID billingId) {

        billingService.processDueBilling(billingId);

        return ResponseEntity.ok(GlobeSuccessResponseBuilder.success(
                "Billing processed", null));
    }
}