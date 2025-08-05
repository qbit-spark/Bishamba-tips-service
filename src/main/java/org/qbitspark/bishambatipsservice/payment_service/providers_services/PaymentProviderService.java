package org.qbitspark.bishambatipsservice.payment_service.providers_services;

import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentProvider;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;

public interface PaymentProviderService {
    PaymentResult processPaymentViaProvider(PaymentRequest request, PaymentProvider provider);
    PaymentResult checkPaymentStatusViaProvider(String transactionRef, PaymentProvider provider);
    boolean isPaymentProviderAvailable(PaymentProvider provider);

}
