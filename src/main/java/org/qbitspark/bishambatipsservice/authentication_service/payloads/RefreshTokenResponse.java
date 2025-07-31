package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import lombok.Data;

@Data
public class RefreshTokenResponse {
    String newToken;
}
