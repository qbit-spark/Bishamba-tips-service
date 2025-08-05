package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemboBalanceRequest {

    @JsonProperty("accountNo")
    private String accountNo; // Wallet account number to check balance

    // Static factory method
    public static TemboBalanceRequest create(String accountNo) {
        return TemboBalanceRequest.builder()
                .accountNo(accountNo)
                .build();
    }
}