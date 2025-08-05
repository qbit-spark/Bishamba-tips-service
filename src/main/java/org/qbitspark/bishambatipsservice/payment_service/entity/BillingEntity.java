package org.qbitspark.bishambatipsservice.payment_service.entity;// ===============================
// BILLING ENTITY - RECURRING PAYMENTS
// ===============================

import jakarta.persistence.*;
import lombok.*;
import org.qbitspark.bishambatipsservice.payment_service.enums.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Entity
@Table(name = "billings_tb", indexes = {
        @Index(name = "idx_billing_customer_id", columnList = "customerId"),
        @Index(name = "idx_billing_service_type", columnList = "serviceType"),
        @Index(name = "idx_billing_status", columnList = "status"),
        @Index(name = "idx_billing_next_billing_date", columnList = "nextBillingDate"),
        @Index(name = "idx_billing_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class BillingEntity {

    // ===============================
    // PRIMARY IDENTIFIERS
    // ===============================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "billing_reference", unique = true, nullable = false, length = 100)
    private String billingReference; // Unique billing reference

    // ===============================
    // CUSTOMER INFORMATION
    // ===============================

    @Column(name = "customer_id", nullable = false)
    private UUID customerId; // Farmer ID

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    // ===============================
    // SERVICE/BILLING DETAILS
    // ===============================

    @Column(name = "service_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType; // WEEKLY_CONSULTATION, MONTHLY_TIPS, etc.

    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName; // Human readable service name

    @Column(name = "service_description", length = 500)
    private String serviceDescription;

    // ===============================
    // BILLING SCHEDULE
    // ===============================

    @Column(name = "billing_frequency", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BillingFrequency billingFrequency; // WEEKLY, MONTHLY, QUARTERLY

    @Column(name = "billing_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal billingAmount; // Amount to charge each cycle

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "billing_day")
    private Integer billingDay; // Day of week (1-7) or day of month (1-31)

    // ===============================
    // BILLING LIFECYCLE
    // ===============================

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BillingStatus status = BillingStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // When billing started

    @Column(name = "end_date")
    private LocalDate endDate; // When billing should end (optional)

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate; // Next payment due date

    @Column(name = "last_billing_date")
    private LocalDate lastBillingDate; // Last successful payment

    // ===============================
    // PAYMENT TRACKING
    // ===============================

    @Column(name = "total_payments_made")
    @Builder.Default
    private Integer totalPaymentsMade = 0; // Count of successful payments

    @Column(name = "total_amount_paid", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmountPaid = BigDecimal.ZERO; // Sum of all payments

    @Column(name = "failed_payment_count")
    @Builder.Default
    private Integer failedPaymentCount = 0; // Count of failed payment attempts

    @Column(name = "last_payment_id")
    private UUID lastPaymentId; // Last payment transaction ID

    // ===============================
    // BUSINESS RULES
    // ===============================

    @Column(name = "max_retry_attempts")
    @Builder.Default
    private Integer maxRetryAttempts = 3; // Max retries for failed payments

    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 7; // Days before service suspension

    @Column(name = "auto_suspend_on_failure")
    @Builder.Default
    private Boolean autoSuspendOnFailure = true; // Auto suspend after max failures

    // ===============================
    // SERVICE PROVIDER
    // ===============================

    @Column(name = "agent_id")
    private UUID agentId; // Agent providing the service

    @Column(name = "service_provider_id")
    private UUID serviceProviderId; // Service provider (could be different from agent)

    // ===============================
    // TIMESTAMPS
    // ===============================

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ===============================
    // ADDITIONAL TRACKING
    // ===============================

    @Column(name = "notes", length = 1000)
    private String notes; // Admin notes

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // Additional flexible data

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
            status = BillingStatus.ACTIVE;
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (nextBillingDate == null) {
            nextBillingDate = calculateNextBillingDate(startDate);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===============================
    // BUSINESS METHODS
    // ===============================

    public void recordSuccessfulPayment(UUID paymentId, BigDecimal amount) {
        this.lastPaymentId = paymentId;
        this.lastBillingDate = LocalDate.now();
        this.totalPaymentsMade++;
        this.totalAmountPaid = this.totalAmountPaid.add(amount);
        this.failedPaymentCount = 0; // Reset failure count
        this.nextBillingDate = calculateNextBillingDate(this.lastBillingDate);

        // Reactivate if was suspended
        if (this.status == BillingStatus.SUSPENDED) {
            this.status = BillingStatus.ACTIVE;
            this.suspendedAt = null;
        }
    }

    public void recordFailedPayment() {
        this.failedPaymentCount++;

        // Auto suspend if max failures reached
        if (autoSuspendOnFailure && failedPaymentCount >= maxRetryAttempts) {
            suspend("Max payment failures reached");
        }
    }

    public void suspend(String reason) {
        this.status = BillingStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.notes = (this.notes != null ? this.notes + "; " : "") + "Suspended: " + reason;
    }

    public void cancel(String reason) {
        this.status = BillingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.notes = (this.notes != null ? this.notes + "; " : "") + "Cancelled: " + reason;
    }

    public void reactivate() {
        this.status = BillingStatus.ACTIVE;
        this.suspendedAt = null;
        this.failedPaymentCount = 0;
    }

    // Status check methods
    public boolean isActive() { return BillingStatus.ACTIVE.equals(this.status); }
    public boolean isSuspended() { return BillingStatus.SUSPENDED.equals(this.status); }
    public boolean isCancelled() { return BillingStatus.CANCELLED.equals(this.status); }
    public boolean isPaused() { return BillingStatus.PAUSED.equals(this.status); }

    // Check if payment is due
    public boolean isDue() {
        return isActive() && nextBillingDate != null &&
                (LocalDate.now().isEqual(nextBillingDate) || LocalDate.now().isAfter(nextBillingDate));
    }

    // Check if payment is overdue
    public boolean isOverdue() {
        return isDue() && LocalDate.now().isAfter(nextBillingDate.plusDays(gracePeriodDays));
    }

    // Calculate the next billing date
    private LocalDate calculateNextBillingDate(LocalDate fromDate) {
        return switch (billingFrequency) {
            case WEEKLY -> fromDate.plusWeeks(1);
            case MONTHLY -> fromDate.plusMonths(1);
            case QUARTERLY -> fromDate.plusMonths(3);
            case YEARLY -> fromDate.plusYears(1);
            default -> fromDate.plusWeeks(1); // Default to weekly
        };
    }

    // Get days until next billing
    public long getDaysUntilNextBilling() {
        if (nextBillingDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), nextBillingDate);
    }

    public void softDelete(UUID userId, String reason) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedByUserId = userId;
        this.notes = (this.notes != null ? this.notes + "; " : "") + "Deleted: " + reason;
    }
}



