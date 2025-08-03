package org.qbitspark.bishambatipsservice.farmer_mng_service.repo;

import org.qbitspark.bishambatipsservice.farmer_mng_service.entities.FarmerEntity;
import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FarmerRepository extends JpaRepository<FarmerEntity, UUID> {

    // Basic queries
    Optional<FarmerEntity> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    List<FarmerEntity> findByAgent(AccountEntity agent);
    List<FarmerEntity> findByAgentId(UUID agentId);
    List<FarmerEntity> findByRegion(String region);

    // Payment-related queries
    List<FarmerEntity> findByHasRegistrationPaidTrue();
    List<FarmerEntity> findByHasRegistrationPaidFalse();
}