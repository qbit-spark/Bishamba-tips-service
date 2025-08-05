package org.qbitspark.bishambatipsservice.payment_service.providers_services.paylaod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemboApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;
    private String errorMessage;
    private T data;
    private String transactionId;
    private String transactionRef;
    private String status;

    // Static factory methods
    public static <T> TemboApiResponse<T> success(T data) {
        TemboApiResponse<T> response = new TemboApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> TemboApiResponse<T> failure(String message, String errorCode) {
        TemboApiResponse<T> response = new TemboApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }
}
