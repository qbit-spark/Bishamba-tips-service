package org.qbitspark.bishambatipsservice.FarmerMngService.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class FarmerRegistrationRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+255[0-9]{9}$", message = "Phone number must be valid Tanzanian format (+255XXXXXXXXX)")
    private String phoneNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String middleName;

    @NotBlank(message = "Region is required")
    private String region;

    private List<String> crops;

    private List<String> livestock;

    @Positive(message = "Farm size must be positive")
    private Double farmSize;
}