package org.qbitspark.bishambatipsservice.FarmerMngService.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FarmerRegistrationResponse {
    private String farmerId;
    private String farmerName;
    private String phoneNumber;
    private String region;
}