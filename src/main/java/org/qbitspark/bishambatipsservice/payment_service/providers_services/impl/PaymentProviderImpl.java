package org.qbitspark.bishambatipsservice.payment_service.providers_services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentProvider;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentRequest;
import org.qbitspark.bishambatipsservice.payment_service.paylaod.PaymentResult;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.PaymentProviderService;
import org.qbitspark.bishambatipsservice.payment_service.providers_services.impl.providers_helpers.EnhancedTemboPaymentProvider;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentProviderImpl implements PaymentProviderService {

    private final EnhancedTemboPaymentProvider temboPaymentProvider;
    // Add other providers as needed
    // private final CardProcessorProvider cardProcessorProvider;
    // private final BankPaymentProvider bankPaymentProvider;

    @Override
    public PaymentResult processPaymentViaProvider(PaymentRequest request, PaymentProvider provider) {
        log.info("Processing payment with provider: {} for transaction: {}",
                provider, request.getTransactionRef());

        return switch (provider) {
            case TEMBO -> temboPaymentProvider.processPayment(request);

            case CASH -> {
                log.warn("Cash payment processing not yet implemented for ref: {}",
                        request.getTransactionRef());
                yield PaymentResult.failure("Cash payment provider not implemented");

            }

            case BANK -> {
                // Implement bank transfer logic or call bank provider
                log.warn("Bank payment processing not yet implemented for ref: {}",
                        request.getTransactionRef());
                yield PaymentResult.failure("Bank payment provider not implemented");
            }

            case CARD_PROCESSOR -> {
                // Call card processor provider
                log.warn("Card processor not yet implemented for ref: {}",
                        request.getTransactionRef());
                yield PaymentResult.failure("Card processor not implemented");
            }

            default -> {
                log.error("Unsupported payment provider: {}", provider);
                yield PaymentResult.failure("Unsupported payment provider: " + provider);
            }
        };
    }

    @Override
    public PaymentResult checkPaymentStatusViaProvider(String transactionRef, PaymentProvider provider) {
        log.info("Checking status with provider: {} for transaction: {}", provider, transactionRef);

        return switch (provider) {
            case TEMBO -> temboPaymentProvider.checkStatus(transactionRef);

            case CASH -> {
                // Cash payments are typically immediate
                yield PaymentResult.failure("Cash status check not implemented");
            }

            case BANK -> {
                // Implement bank status check
                yield PaymentResult.failure("Bank status check not implemented");
            }

            case CARD_PROCESSOR -> {
                // Implement card processor status check
                yield PaymentResult.failure("Card processor status check not implemented");
            }

            default -> PaymentResult.failure("Unsupported provider for status check: " + provider);
        };
    }

    @Override
    public boolean isPaymentProviderAvailable(PaymentProvider provider) {
        return switch (provider) {
            case TEMBO -> temboPaymentProvider.isAvailable();
            case CASH -> true; // Not implemented yet
            case BANK -> false; // Not implemented yet
            case CARD_PROCESSOR -> false; // Not implemented yet
            default -> false;
        };
    }
}