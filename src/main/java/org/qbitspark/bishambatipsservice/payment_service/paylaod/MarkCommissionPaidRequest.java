package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MarkCommissionPaidRequest {

    @NotNull(message = "Payout transaction ID is required")
    private UUID payoutTransactionId;

    private String payoutReference;

    private String notes;
}