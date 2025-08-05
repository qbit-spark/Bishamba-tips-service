package org.qbitspark.bishambatipsservice.payment_service.providers_services;

import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;

public interface PaymentProvider {
    PaymentResult processPayment(PaymentRequest request);
    PaymentResult checkStatus(String transactionRef);
    boolean isAvailable();

}
