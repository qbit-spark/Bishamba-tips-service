package org.qbitspark.bishambatipsservice.payment_service.repo;

import org.qbitspark.bishambatipsservice.payment_service.entity.CommissionEntity;
import org.qbitspark.bishambatipsservice.payment_service.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CommissionRepository extends JpaRepository<CommissionEntity, UUID> {

    List<CommissionEntity> findByAgentId(UUID agentId);

    List<CommissionEntity> findByStatus(CommissionStatus status);

    List<CommissionEntity> findByStatusAndDueDateBefore(CommissionStatus status, LocalDateTime dateTime);

    List<CommissionEntity> findByAgentIdAndStatus(UUID agentId, CommissionStatus status);

    boolean existsByPaymentId(UUID paymentId);
}