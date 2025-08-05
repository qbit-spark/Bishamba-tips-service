package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemboStatementResponse {
    private boolean success;
    private String message;
    private List<TemboTransaction> transactions;
    private Integer totalCount;
    private BigDecimal totalAmount;
    private String currency;
}

