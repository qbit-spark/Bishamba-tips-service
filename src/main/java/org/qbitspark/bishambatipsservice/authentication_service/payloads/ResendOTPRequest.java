package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOTPRequest {

    @NotBlank(message = "Temp token is mandatory")
    private String tempToken;
}