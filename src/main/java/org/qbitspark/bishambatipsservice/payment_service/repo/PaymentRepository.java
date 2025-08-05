package org.qbitspark.bishambatipsservice.payment_service.repo;

import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByTransactionRef(String transactionRef);

    List<PaymentEntity> findByCustomerId(UUID customerId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    List<PaymentEntity> findByRecordedByAgentId(UUID agentId);

    List<PaymentEntity> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    boolean existsByTransactionRef(String transactionRef);

    Optional<PaymentEntity> findByExternalTransactionId(String externalTransactionId);
}