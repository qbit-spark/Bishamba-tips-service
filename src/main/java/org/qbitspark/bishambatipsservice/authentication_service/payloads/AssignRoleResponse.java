package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AssignRoleResponse {
    private UUID accountId;
    private String userName;
    private String email;
    private String assignedRole;
    private boolean isApproved;
    private String message;
}