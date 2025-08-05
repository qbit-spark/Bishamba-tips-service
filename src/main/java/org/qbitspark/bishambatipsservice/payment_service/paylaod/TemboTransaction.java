package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemboTransaction {
    private String transactionId;
    private String transactionRef;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String narration;
    private LocalDateTime transactionDate;
    private String counterParty;
}
