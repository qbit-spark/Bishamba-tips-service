package org.qbitspark.bishambatipsservice.FarmerMngService.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerResponse {
    private UUID id;
    private String phoneNumber; // Will be masked for non-admins
    private String firstName;
    private String lastName;
    private String middleName;
    private String region;
    private List<String> crops;
    private List<String> livestock;
    private Double farmSize;
    private boolean isActive;
    private boolean hasRegistrationPaid;
    private LocalDateTime registrationPaidAt;
    private LocalDateTime createdAt;

    // Helper method to mask phone number
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }
        // +255123456789 -> +255***6789
        return phoneNumber.substring(0, 4) + "***" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}