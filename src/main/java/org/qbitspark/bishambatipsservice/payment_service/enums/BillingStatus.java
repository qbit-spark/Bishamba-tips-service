package org.qbitspark.bishambatipsservice.payment_service.enums;

public enum BillingStatus {
    ACTIVE,     // Active billing, payments being collected
    SUSPENDED,  // Suspended due to failed payments
    PAUSED,     // Temporarily paused by user/admin
    CANCELLED,  // Permanently cancelled
    EXPIRED     // Billing period ended
}
