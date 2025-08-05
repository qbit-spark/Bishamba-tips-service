package org.qbitspark.bishambatipsservice.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.qbitspark.bishambatipsservice.payment_service.enums.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments_tb", indexes = {
        @Index(name = "idx_payment_transaction_ref", columnList = "transactionRef", unique = true),
        @Index(name = "idx_payment_customer_id", columnList = "customerId"),
        @Index(name = "idx_payment_agent_id", columnList = "recordedByAgentId"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "paymentMethod"),
        @Index(name = "idx_payment_type", columnList = "paymentType"),
        @Index(name = "idx_payment_created_at", columnList = "createdAt"),
        @Index(name = "idx_payment_external_ref", columnList = "externalTransactionId"),
        @Index(name = "idx_payment_provider", columnList = "paymentProvider")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    // ===============================
    // PRIMARY IDENTIFIERS
    // ===============================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_ref", unique = true, nullable = false, length = 100)
    private String transactionRef;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId; // Tembo transaction ID, bank ref, etc.

    @Column(name = "internal_transaction_reference_id", length = 100)
    private UUID internalTransactionId;


    // ===============================
    // CUSTOMER/PAYER INFORMATION
    // ===============================

    @Column(name = "customer_id")
    private UUID customerId; // Farmer ID, User ID, etc.

    @Column(name = "customer_type", length = 50)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType; // FARMER, AGENT, ADMIN, GUEST

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    // ===============================
    // PAYMENT CLASSIFICATION
    // ===============================

    @Column(name = "payment_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType; // FARMER_REGISTRATION, PRODUCT_PURCHASE, etc.

    @Column(name = "payment_method", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // USSD_PUSH, CASH, BANK_TRANSFER, etc.

    @Column(name = "payment_direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentDirection direction; // INBOUND, OUTBOUND

    @Column(name = "payment_provider", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider; // TEMBO, BANK, CASH, etc.

    // ===============================
    // FINANCIAL INFORMATION
    // ===============================

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode; // TZS, USD, etc.

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate; // For multi-currency support

    @Column(name = "base_amount", precision = 15, scale = 2)
    private BigDecimal baseAmount; // Amount in base currency (TZS)

    @Column(name = "fees", precision = 10, scale = 2)
    private BigDecimal fees; // Transaction fees

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount; // amount + fees

    // ===============================
    // PAYMENT STATUS & LIFECYCLE
    // ===============================

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retry_attempts")
    @Builder.Default
    private Integer maxRetryAttempts = 3;

    // ===============================
    // AGENT/PROCESSOR INFORMATION
    // ===============================

    @Column(name = "recorded_by_agent_id")
    private UUID recordedByAgentId; // Agent who processed the payment

    @Column(name = "processed_by_user_id")
    private UUID processedByUserId; // System user who initiated

    // ===============================
    // PROVIDER-SPECIFIC DATA
    // ===============================

    @Column(name = "provider_request_id", length = 100)
    private String providerRequestId; // Request ID sent to provider

    @Column(name = "provider_response_code", length = 50)
    private String providerResponseCode;

    @Column(name = "provider_response_message", length = 500)
    private String providerResponseMessage;

    @Column(name = "provider_fee", precision = 10, scale = 2)
    private BigDecimal providerFee; // Fee charged by provider

    @Column(name = "provider_reference", length = 100)
    private String providerReference; // Provider's internal reference

    // ===============================
    // CALLBACK & WEBHOOK DATA
    // ===============================

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;

    @Column(name = "callback_received")
    @Builder.Default
    private Boolean callbackReceived = false;

    @Column(name = "callback_count")
    @Builder.Default
    private Integer callbackCount = 0;

    @Column(name = "webhook_data", columnDefinition = "TEXT")
    private String webhookData; // Raw webhook response

    @Column(name = "callback_processed_at")
    private LocalDateTime callbackProcessedAt;

    // ===============================
    // TIMESTAMPS & AUDIT
    // ===============================

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt; // When payment was initiated

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Payment expiration time

    // ===============================
    // ADDITIONAL METADATA
    // ===============================

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // JSON for additional flexible data

    // ===============================
    // NOTIFICATION & COMMUNICATION
    // ===============================

    @Column(name = "notification_sent")
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(name = "receipt_generated")
    @Builder.Default
    private Boolean receiptGenerated = false;

    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(name = "receipt_sent")
    @Builder.Default
    private Boolean receiptSent = false;

    // ===============================
    // SOFT DELETE SUPPORT
    // ===============================

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_user_id")
    private UUID deletedByUserId;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    // ===============================
    // LIFECYCLE METHODS
    // ===============================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (initiatedAt == null) {
            initiatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (callbackCount == null) {
            callbackCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===============================
    // BUSINESS METHODS
    // ===============================

    public void markAsProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null) ? this.retryCount + 1 : 1;
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetryAttempts &&
                this.status == PaymentStatus.FAILED;
    }

    public void processCallback(String webhookData) {
        this.callbackReceived = true;
        this.callbackCount = (this.callbackCount != null) ? this.callbackCount + 1 : 1;
        this.webhookData = webhookData;
        this.callbackProcessedAt = LocalDateTime.now();
    }

    public void softDelete(UUID userId, String reason) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedByUserId = userId;
        this.deletionReason = reason;
    }

    // Status check methods
    public boolean isPending() { return PaymentStatus.PENDING.equals(this.status); }
    public boolean isProcessing() { return PaymentStatus.PROCESSING.equals(this.status); }
    public boolean isCompleted() { return PaymentStatus.COMPLETED.equals(this.status); }
    public boolean isFailed() { return PaymentStatus.FAILED.equals(this.status); }
    public boolean isCancelled() { return PaymentStatus.CANCELLED.equals(this.status); }
    public boolean isRefunded() { return PaymentStatus.REFUNDED.equals(this.status); }

    // Direction check methods
    public boolean isInbound() { return PaymentDirection.INBOUND.equals(this.direction); }
    public boolean isOutbound() { return PaymentDirection.OUTBOUND.equals(this.direction); }

    // Financial calculations
    public BigDecimal getNetAmount() {
        BigDecimal net = amount;
        if (fees != null) {
            net = net.subtract(fees);
        }
        if (providerFee != null) {
            net = net.subtract(providerFee);
        }
        return net;
    }

    public BigDecimal getTotalFees() {
        BigDecimal total = BigDecimal.ZERO;
        if (fees != null) {
            total = total.add(fees);
        }
        if (providerFee != null) {
            total = total.add(providerFee);
        }
        return total;
    }
}


