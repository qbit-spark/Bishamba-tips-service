package org.qbitspark.bishambatipsservice.payment_service.service;

import org.qbitspark.bishambatipsservice.payment_service.entity.PaymentEntity;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    PaymentResult initiatePayment(PaymentRequest request);

    PaymentResult processCallback(String transactionRef, Map<String, Object> callbackData);

    PaymentEntity getPaymentByTransactionRef(String transactionRef);

    List<PaymentEntity> getPaymentsByCustomer(UUID customerId);

    PaymentResult retryFailedPayment(String transactionRef);

    PaymentResult checkPaymentStatus(String transactionRef);
}