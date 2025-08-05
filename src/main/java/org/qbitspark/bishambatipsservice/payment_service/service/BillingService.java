package org.qbitspark.bishambatipsservice.payment_service.service;

import org.qbitspark.bishambatipsservice.payment_service.entity.BillingEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BillingService {

    BillingEntity createBilling(UUID customerId, String serviceName, BigDecimal amount);

    List<BillingEntity> getDueBillings();

    void processDueBilling(UUID billingId);

    void suspendBilling(UUID billingId, String reason);

    void reactivateBilling(UUID billingId);

    List<BillingEntity> getCustomerBillings(UUID customerId);
}