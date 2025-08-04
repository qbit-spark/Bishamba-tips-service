package org.qbitspark.bishambatipsservice.farmer_mng_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;


@Data
public class ConfirmTermsRequest {

    @NotBlank(message = "Terms agreement code is required")
    @Pattern(regexp = "^[A-Z0-9]{3}-[A-Z0-9]{3}$", message = "Terms agreement code must be in format XXX-XXX (3 characters, hyphen, 3 characters)")
    private String termsAgreementCode;
}