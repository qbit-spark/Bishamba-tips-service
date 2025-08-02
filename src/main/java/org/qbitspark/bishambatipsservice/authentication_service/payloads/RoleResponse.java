package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RoleResponse {
    private UUID roleId;
    private String roleName;
}