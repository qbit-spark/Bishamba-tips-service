package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuspendBillingRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}