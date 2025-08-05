package org.qbitspark.bishambatipsservice.payment_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.entity.BillingEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.*;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;
import org.qbitspark.bishambatipsservice.payment_service.repo.BillingRepository;
import org.qbitspark.bishambatipsservice.payment_service.service.BillingService;
import org.qbitspark.bishambatipsservice.payment_service.service.PaymentService;
import org.qbitspark.bishambatipsservice.payment_service.utils.PaymentUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final PaymentService paymentService;
    private final PaymentUtils paymentUtils;

    @Override
    @Transactional
    public BillingEntity createBilling(UUID customerId, String serviceName, BigDecimal amount) {
        log.info("Creating billing for customer: {}, service: {}", customerId, serviceName);

        String billingReference = "BILL_" + System.currentTimeMillis();

        BillingEntity billing = BillingEntity.builder()
                .billingReference(billingReference)
                .customerId(customerId)
                .customerName("Customer") // TODO: Get actual name
                .serviceType(ServiceType.WEEKLY_CONSULTATION)
                .serviceName(serviceName)
                .serviceDescription("Weekly farming consultation service")
                .billingFrequency(BillingFrequency.WEEKLY)
                .billingAmount(amount)
                .currencyCode("TZS")
                .billingDay(5) // Friday
                .status(BillingStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(getNextFriday())
                .paymentProvider(PaymentProvider.TEMBO)
                .createdAt(LocalDateTime.now())
                .build();

        BillingEntity saved = billingRepository.save(billing);

        log.info("Billing created: {} for customer: {}", billingReference, customerId);
        return saved;
    }

    @Override
    public List<BillingEntity> getDueBillings() {
        return billingRepository.findByStatusAndNextBillingDateBefore(
                BillingStatus.ACTIVE, LocalDate.now().plusDays(1));
    }

    @Override
    @Transactional
    public void processDueBilling(UUID billingId) {
        BillingEntity billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing not found"));

        if (!billing.isDue()) {
            log.warn("Billing not due yet: {}", billing.getBillingReference());
            return;
        }

        log.info("Processing due billing: {}", billing.getBillingReference());

        // Create payment request
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .transactionRef(paymentUtils.generateTransactionRef(PaymentType.SERVICE_FEE))
                .customerId(billing.getCustomerId())
                .customerName(billing.getCustomerName())
                .customerPhone(billing.getCustomerPhone())
                .paymentType(PaymentType.SERVICE_FEE)
                .paymentMethod(PaymentMethod.USSD_PUSH)
                .direction(PaymentDirection.INBOUND)
                .amount(billing.getBillingAmount())
                .currencyCode(billing.getCurrencyCode())
                .description("Billing for " + billing.getServiceName())
                .build();

        // Initiate payment
        PaymentResult result = paymentService.initiatePayment(paymentRequest);

        if (result.isSuccess()) {
            log.info("Billing payment initiated: {}", billing.getBillingReference());
        } else {
            billing.recordFailedPayment();
            billingRepository.save(billing);
            log.error("Billing payment failed: {}", billing.getBillingReference());
        }
    }

    @Override
    @Transactional
    public void suspendBilling(UUID billingId, String reason) {
        BillingEntity billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing not found"));

        billing.suspend(reason);
        billingRepository.save(billing);

        log.info("Billing suspended: {} - {}", billing.getBillingReference(), reason);
    }

    @Override
    @Transactional
    public void reactivateBilling(UUID billingId) {
        BillingEntity billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new RuntimeException("Billing not found"));

        billing.reactivate();
        billingRepository.save(billing);

        log.info("Billing reactivated: {}", billing.getBillingReference());
    }

    @Override
    public List<BillingEntity> getCustomerBillings(UUID customerId) {
        return billingRepository.findByCustomerId(customerId);
    }

    private LocalDate getNextFriday() {
        LocalDate today = LocalDate.now();
        int daysUntilFriday = (5 - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilFriday == 0) daysUntilFriday = 7; // If today is Friday, next Friday
        return today.plusDays(daysUntilFriday);
    }
}