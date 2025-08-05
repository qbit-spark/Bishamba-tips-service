package org.qbitspark.bishambatipsservice.payment_service.utils;

import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.payment_service.enums.PaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

@Component
@Slf4j
public class PaymentUtils {

    // ===============================
    // PHONE NUMBER FORMATTING
    // ===============================

    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Remove any spaces or special characters
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Convert from different formats to 255XXXXXXXXX
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "255" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("+255")) {
            phoneNumber = phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("255")) {
            // Already in the correct format
        } else if (phoneNumber.length() == 9) {
            phoneNumber = "255" + phoneNumber;
        }

        return phoneNumber;
    }

    public boolean isValidTanzanianPhoneNumber(String phoneNumber) {
        phoneNumber = formatPhoneNumber(phoneNumber);
        return phoneNumber != null && phoneNumber.matches("^255[67][0-9]{8}$");
    }

    // ===============================
    // SERVICE PROVIDER DETECTION
    // ===============================

    public String detectServiceProvider(String phoneNumber) {
        phoneNumber = formatPhoneNumber(phoneNumber);

        if (phoneNumber == null || phoneNumber.length() != 12) {
            return "UNKNOWN";
        }

        String prefix = phoneNumber.substring(3, 6);

        return switch (prefix) {
            case "620", "621", "622", "623", "624", "625", "626", "627", "628", "629" -> "HALOTEL";
            case "650", "651", "652", "653", "654", "655", "656", "657", "658", "659" -> "TIGO";
            case "680", "681", "682", "683", "684", "685", "686", "687", "688", "689" -> "AIRTEL";
            case "740", "741", "742", "743", "744", "745", "746", "747", "748", "749", "750", "751", "752", "753",
                 "754", "755", "756", "757", "758", "759" -> "VODACOM";
            default -> "UNKNOWN";
        };
    }

    // ===============================
    // TEMBO CHANNEL MAPPING
    // ===============================

    public String getTemboC2BChannel(String phoneNumber) {
        String provider = detectServiceProvider(phoneNumber);

        return switch (provider) {
            case "HALOTEL" -> "TZ-HALOTEL-C2B";
            case "TIGO" -> "TZ-TIGO-C2B";
            case "AIRTEL" -> "TZ-AIRTEL-C2B";
            case "VODACOM" -> "TZ-VODACOM-C2B";
            default -> "UNKNOWN";

        };
    }

    public String getTemboB2CServiceCode(String phoneNumber) {
        String provider = detectServiceProvider(phoneNumber);

        return switch (provider) {
            case "HALOTEL" -> "TZ-HALOTEL-B2C";
            case "TIGO" -> "TZ-TIGO-B2C";
            case "AIRTEL" -> "TZ-AIRTEL-B2C";
            case "VODACOM" -> "TZ-VODACOM-B2C";
            default -> "UNKNOWN";
        };
    }

    // ===============================
    // TRANSACTION REFERENCE GENERATION
    // ===============================

    public String generateTransactionRef(PaymentType paymentType) {
        String prefix = getTransactionPrefix(paymentType);
        return prefix + "_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getTransactionPrefix(PaymentType paymentType) {
        return switch (paymentType) {
            case FARMER_REGISTRATION -> "REG";
            case PRODUCT_PURCHASE -> "PUR";
            case SERVICE_FEE -> "SVC";
            case COMMISSION_PAYOUT -> "COM";
            case AGENT_WITHDRAWAL -> "WTH";
            case REFUND -> "REF";
            default -> "PAY";
        };
    }

    // ===============================
    // AMOUNT VALIDATION
    // ===============================

    public boolean isValidAmount(BigDecimal amount, PaymentType paymentType) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Payment-type-specific validation
        return switch (paymentType) {
            case FARMER_REGISTRATION -> amount.compareTo(BigDecimal.valueOf(5000)) >= 0 &&
                    amount.compareTo(BigDecimal.valueOf(50000)) <= 0;
            case PRODUCT_PURCHASE -> amount.compareTo(BigDecimal.valueOf(1000)) >= 0 &&
                    amount.compareTo(BigDecimal.valueOf(1000000)) <= 0;
            default ->
                // General validation
                    amount.compareTo(BigDecimal.valueOf(100)) >= 0 &&
                            amount.compareTo(BigDecimal.valueOf(5000000)) <= 0;
        };
    }

    // ===============================
    // CURRENCY FORMATTING
    // ===============================

    public String formatAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) return "0";

        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount) + " " + currencyCode;
    }

    public String formatAmountTZS(BigDecimal amount) {
        return formatAmount(amount, "TZS");
    }

    // ===============================
    // PHONE NUMBER MASKING (for security)
    // ===============================

    public String maskPhoneNumber(String phoneNumber) {
        phoneNumber = formatPhoneNumber(phoneNumber);

        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "***";
        }

        // Show the first 6 digits and last 2 digits: 255745****50
        return phoneNumber.substring(0, 6) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}