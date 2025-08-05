package org.qbitspark.bishambatipsservice.payment_service.providers_services;

import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentProvider;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;

public interface PaymentProviderService {
    PaymentResult processPayment(PaymentRequest request, PaymentProvider provider);
    PaymentResult checkStatus(String transactionRef, PaymentProvider provider);
    boolean isAvailable(PaymentProvider provider);

}
