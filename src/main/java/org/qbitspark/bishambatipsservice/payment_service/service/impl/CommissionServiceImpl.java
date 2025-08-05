package org.qbitspark.bishambatipsservice.payment_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.entity.CommissionEntity;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.CommissionStatus;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentMethod;
import org.qbitspark.bishambatipsservice.payment_service.repo.CommissionRepository;
import org.qbitspark.bishambatipsservice.payment_service.service.CommissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionServiceImpl implements CommissionService {

    private final CommissionRepository commissionRepository;

    @Override
    @Transactional
    public CommissionEntity calculateCommission(PaymentEntity payment) {
        log.info("Calculating commission for payment: {}", payment.getTransactionRef());

        // Check if commission already calculated
        if (commissionRepository.existsByPaymentId(payment.getId())) {
            log.warn("Commission already exists for payment: {}", payment.getTransactionRef());
            return null;
        }

        // Skip if no agent or invalid payment type
        if (payment.getRecordedByAgentId() == null || !shouldEarnCommission(payment)) {
            log.info("No commission applicable for payment: {}", payment.getTransactionRef());
            return null;
        }

        // Calculate commission rate and amount
        BigDecimal commissionRate = getCommissionRate(payment);
        BigDecimal commissionAmount = payment.getAmount()
                .multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Create commission entity
        CommissionEntity commission = CommissionEntity.builder()
                .agentId(payment.getRecordedByAgentId())
                .paymentId(payment.getId())
                .commissionAmount(commissionAmount)
                .commissionRate(commissionRate)
                .baseAmount(payment.getAmount())
                .currencyCode(payment.getCurrencyCode())
                .status(CommissionStatus.CALCULATED)
                .payoutMethod(PaymentMethod.MOBILE_MONEY) // Default
                .paymentType(payment.getPaymentType())
                .customerId(payment.getCustomerId())
                .customerName(payment.getCustomerName())
                .createdAt(LocalDateTime.now())
                .build();

        CommissionEntity saved = commissionRepository.save(commission);

        log.info("Commission calculated: {} {} for agent: {}",
                commissionAmount, payment.getCurrencyCode(), payment.getRecordedByAgentId());

        return saved;
    }

    @Override
    public List<CommissionEntity> getDueCommissions() {
        return commissionRepository.findByStatusAndDueDateBefore(
                CommissionStatus.CALCULATED, LocalDateTime.now());
    }

    @Override
    public List<CommissionEntity> getAgentCommissions(UUID agentId) {
        return commissionRepository.findByAgentId(agentId);
    }

    @Override
    @Transactional
    public void markCommissionAsPaid(UUID commissionId, UUID payoutTransactionId) {
        CommissionEntity commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new RuntimeException("Commission not found"));

        commission.markAsPaid(payoutTransactionId, "PAYOUT_" + System.currentTimeMillis());
        commissionRepository.save(commission);

        log.info("Commission marked as paid: {}", commissionId);
    }

    // Helper methods
    private boolean shouldEarnCommission(PaymentEntity payment) {
        // Only farmer registration and product purchases earn commission
        return switch (payment.getPaymentType()) {
            case FARMER_REGISTRATION, PRODUCT_PURCHASE, SERVICE_FEE -> true;
            default -> false;
        };
    }

    private BigDecimal getCommissionRate(PaymentEntity payment) {
        // Different commission rates based on payment type
        return switch (payment.getPaymentType()) {
            case FARMER_REGISTRATION -> new BigDecimal("0.10"); // 10%
            case PRODUCT_PURCHASE -> new BigDecimal("0.05");    // 5%
            case SERVICE_FEE -> new BigDecimal("0.08");         // 8%
            default -> new BigDecimal("0.05");                  // 5% default
        };
    }
}