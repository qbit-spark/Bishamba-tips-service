package org.qbitspark.bishambatipsservice.payment_service.enums;

public enum CommissionStatus {
    CALCULATED,        // Commission calculated but not yet paid
    QUEUED_FOR_PAYOUT, // Queued in batch for payout
    PROCESSING_PAYOUT, // Payout in progress
    PAID,              // Successfully paid to agent
    PAYOUT_FAILED,     // Payout attempt failed
    CANCELLED          // Commission cancelled/voided
}
