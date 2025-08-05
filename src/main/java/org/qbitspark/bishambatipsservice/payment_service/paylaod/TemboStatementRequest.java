package org.qbitspark.bishambatipsservice.payment_service.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemboStatementRequest {

    @JsonProperty("accountNo")
    private String accountNo; // Wallet account number

    @JsonProperty("startDate")
    private String startDate; // Format: yyyy-MM-dd

    @JsonProperty("endDate")
    private String endDate; // Format: yyyy-MM-dd

    // Static factory methods
    public static TemboStatementRequest create(String accountNo, LocalDate startDate, LocalDate endDate) {
        return TemboStatementRequest.builder()
                .accountNo(accountNo)
                .startDate(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .endDate(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }

    public static TemboStatementRequest thisMonth(String accountNo) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        return create(accountNo, startOfMonth, endOfMonth);
    }

    public static TemboStatementRequest lastMonth(String accountNo) {
        LocalDate now = LocalDate.now();
        LocalDate startOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = now.minusMonths(1).withDayOfMonth(startOfLastMonth.lengthOfMonth());

        return create(accountNo, startOfLastMonth, endOfLastMonth);
    }
}