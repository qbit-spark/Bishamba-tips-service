package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import lombok.Data;

@Data
public class VerifiedAccountResponse {

    private Object userData;
    private String accessToken;
    private String refreshToken;

}
