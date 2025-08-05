package org.qbitspark.bishambatipsservice.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.qbitspark.bishambatipsservice.payment_service.enums.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Entity
@Table(name = "commissions_tb", indexes = {
        @Index(name = "idx_commission_agent_id", columnList = "agentId"),
        @Index(name = "idx_commission_payment_id", columnList = "paymentId"),
        @Index(name = "idx_commission_status", columnList = "status"),
        @Index(name = "idx_commission_created_at", columnList = "createdAt"),
        @Index(name = "idx_commission_paid_at", columnList = "paidAt")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionEntity {

    // ===============================
    // PRIMARY IDENTIFIERS
    // ===============================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agent_id", nullable = false)
    private UUID agentId; // Agent earning the commission

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId; // Source payment that generated this commission

    // ===============================
    // COMMISSION DETAILS
    // ===============================

    @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount; // Amount of commission earned

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate; // Rate used (e.g., 0.0500 = 5%)

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmount; // Original payment amount commission was calculated from

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode; // TZS, USD, etc.

    // ===============================
    // COMMISSION STATUS
    // ===============================

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommissionStatus status = CommissionStatus.CALCULATED;

    @Column(name = "payout_method", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod payoutMethod; // How commission will be paid

    @Column(name = "payout_transaction_id")
    private UUID payoutTransactionId; // Payment ID when commission is paid out

    // ===============================
    // BUSINESS CONTEXT
    // ===============================

    @Column(name = "payment_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType; // Type of payment that generated commission

    @Column(name = "customer_id")
    private UUID customerId; // Customer who made the original payment

    @Column(name = "customer_name", length = 255)
    private String customerName; // Customer name for reference

    // ===============================
    // TIMESTAMPS
    // ===============================

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // When commission was calculated

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt; // When commission was actually paid to agent

    @Column(name = "due_date")
    private LocalDateTime dueDate; // When commission should be paid (e.g., next Friday)

    // ===============================
    // PAYOUT TRACKING
    // ===============================

    @Column(name = "payout_batch_id", length = 100)
    private String payoutBatchId; // Batch ID for group payouts

    @Column(name = "payout_reference", length = 100)
    private String payoutReference; // External reference for payout

    @Column(name = "payout_failure_reason", length = 500)
    private String payoutFailureReason; // If payout failed

    // ===============================
    // SOFT DELETE
    // ===============================

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_user_id")
    private UUID deletedByUserId;

    // ===============================
    // LIFECYCLE METHODS
    // ===============================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CommissionStatus.CALCULATED;
        }
        // Set the due date to next Friday by default
        if (dueDate == null) {
            dueDate = calculateNextFriday();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===============================
    // BUSINESS METHODS
    // ===============================

    public void markAsPaid(UUID payoutTransactionId, String payoutReference) {
        this.status = CommissionStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.payoutTransactionId = payoutTransactionId;
        this.payoutReference = payoutReference;
    }

    public void markAsPayoutFailed(String reason) {
        this.status = CommissionStatus.PAYOUT_FAILED;
        this.payoutFailureReason = reason;
    }

    public void markAsQueued(String batchId) {
        this.status = CommissionStatus.QUEUED_FOR_PAYOUT;
        this.payoutBatchId = batchId;
    }

    public void softDelete(UUID userId) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedByUserId = userId;
    }

    // Status check methods
    public boolean isCalculated() { return CommissionStatus.CALCULATED.equals(this.status); }
    public boolean isQueued() { return CommissionStatus.QUEUED_FOR_PAYOUT.equals(this.status); }
    public boolean isPaid() { return CommissionStatus.PAID.equals(this.status); }
    public boolean isPayoutFailed() { return CommissionStatus.PAYOUT_FAILED.equals(this.status); }

    // Check if commission is due for payment
    public boolean isDue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    // Check if the commission is overdue
    public boolean isOverdue() {
        return isDue() && !isPaid();
    }

    // Calculate next Friday for due date
    private LocalDateTime calculateNextFriday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextFriday = now.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
                .withHour(9).withMinute(0).withSecond(0).withNano(0);

        // If today is Friday, and it's before 9 AM, use today
        if (now.getDayOfWeek() == DayOfWeek.FRIDAY && now.getHour() < 9) {
            nextFriday = now.withHour(9).withMinute(0).withSecond(0).withNano(0);
        }

        return nextFriday;
    }
}

