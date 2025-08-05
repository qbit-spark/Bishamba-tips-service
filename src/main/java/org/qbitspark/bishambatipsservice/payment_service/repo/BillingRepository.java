package org.qbitspark.bishambatipsservice.payment_service.repo;

import org.qbitspark.bishambatipsservice.payment_service.entity.BillingEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillingRepository extends JpaRepository<BillingEntity, UUID> {

    List<BillingEntity> findByCustomerId(UUID customerId);

    List<BillingEntity> findByStatus(BillingStatus status);

    List<BillingEntity> findByStatusAndNextBillingDateBefore(BillingStatus status, LocalDate date);

    List<BillingEntity> findByStatusAndNextBillingDate(BillingStatus status, LocalDate date);
}