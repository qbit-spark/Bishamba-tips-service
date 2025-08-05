package org.qbitspark.bishambatipsservice.payment_service.service;

import org.qbitspark.bishambatipsservice.payment_service.entity.CommissionEntity;
import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CommissionService {

    CommissionEntity calculateCommission(PaymentEntity payment);

    List<CommissionEntity> getDueCommissions();

    List<CommissionEntity> getAgentCommissions(UUID agentId);

    void markCommissionAsPaid(UUID commissionId, UUID payoutTransactionId);
}