package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignRoleRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Role ID is required")
    private UUID roleId;
}