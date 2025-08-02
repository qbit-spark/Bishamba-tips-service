package org.qbitspark.bishambatipsservice.authentication_service.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 30, message = "First name should be less than 30 characters")
    private String firstName;

    @Size(max = 30, message = "Last name should be less than 30 characters")
    private String lastName;

    @Size(max = 30, message = "Middle name should be less than 30 characters")
    private String middleName;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(
            regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Phone number must be in valid international format"
    )
    private String phoneNumber;
}