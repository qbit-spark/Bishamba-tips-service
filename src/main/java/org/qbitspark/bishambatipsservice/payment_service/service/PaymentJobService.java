package org.qbitspark.bishambatipsservice.payment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.entity.BillingEntity;
import org.qbitspark.bishambatipsservice.payment_service.entity.CommissionEntity;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentStatus;
import org.qbitspark.bishambatipsservice.payment_service.repo.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentJobService {

    private final BillingService billingService;
    private final CommissionService commissionService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Scheduled(cron = "0 0 9 * * FRI") // Every Friday at 9 AM
    public void processDueBillings() {
        log.info("Starting due billings processing job");

        List<BillingEntity> dueBillings = billingService.getDueBillings();

        for (BillingEntity billing : dueBillings) {
            try {
                billingService.processDueBilling(billing.getId());
            } catch (Exception e) {
                log.error("Failed to process billing: {}", billing.getBillingReference(), e);
            }
        }

        log.info("Due billings processing completed. Processed: {}", dueBillings.size());
    }

    @Scheduled(cron = "0 0 10 * * FRI") // Every Friday at 10 AM
    public void processDueCommissions() {
        log.info("Starting commission payout job");

        List<CommissionEntity> dueCommissions = commissionService.getDueCommissions();

        for (CommissionEntity commission : dueCommissions) {
            try {
                // TODO: Implement commission payout logic
                log.info("Processing commission payout for agent: {}", commission.getAgentId());
            } catch (Exception e) {
                log.error("Failed to process commission: {}", commission.getId(), e);
            }
        }

        log.info("Commission processing completed. Processed: {}", dueCommissions.size());
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void checkPendingPayments() {
        log.debug("Checking pending payments status");

        List<PaymentEntity> pendingPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PROCESSING, LocalDateTime.now().minusMinutes(10));

        for (PaymentEntity payment : pendingPayments) {
            try {
                paymentService.checkPaymentStatus(payment.getTransactionRef());
            } catch (Exception e) {
                log.error("Failed to check payment status: {}",
                        payment.getTransactionRef(), e);
            }
        }

        if (!pendingPayments.isEmpty()) {
            log.info("Checked {} pending payments", pendingPayments.size());
        }
    }

    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    public void retryFailedPayments() {
        log.info("Starting failed payment retry job");

        List<PaymentEntity> failedPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.FAILED, LocalDateTime.now().minusHours(1));

        int retryCount = 0;
        for (PaymentEntity payment : failedPayments) {
            if (payment.canRetry()) {
                try {
                    paymentService.retryFailedPayment(payment.getTransactionRef());
                    retryCount++;
                } catch (Exception e) {
                    log.error("Failed to retry payment: {}",
                            payment.getTransactionRef(), e);
                }
            }
        }

        log.info("Failed payment retry completed. Retried: {}", retryCount);
    }
}