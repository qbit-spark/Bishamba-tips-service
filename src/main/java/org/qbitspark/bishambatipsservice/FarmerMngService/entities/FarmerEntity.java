package org.qbitspark.bishambatipsservice.FarmerMngService.entities;

import org.qbitspark.bishambatipsservice.FarmerMngService.utils.StringListConverter;
import org.qbitspark.bishambatipsservice.authentication_service.entity.AccountEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "farmers_tb", indexes = {
        @Index(name = "idx_phone_number", columnList = "phoneNumber"),
        @Index(name = "idx_agent_id", columnList = "agent_id"),
        @Index(name = "idx_region", columnList = "region")
})
public class FarmerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String middleName;

    @Column(nullable = false)
    private String region;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<String> crops;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<String> livestock;

    private Double farmSize; // in acres

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AccountEntity agent;

    private boolean isActive = true;

    private boolean hasRegistrationPaid = false;

    private LocalDateTime registrationPaidAt;

    private LocalDateTime lastWeeklyPayment;

    private LocalDateTime nextPaymentDue;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime editedAt;
}