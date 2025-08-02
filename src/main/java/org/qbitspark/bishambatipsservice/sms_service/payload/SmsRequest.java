package org.qbitspark.bishambatipsservice.sms_service.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String from;
    private String to;
    private String text;
    private String reference;

    public static SmsRequest createOtpSms(String senderId, String phoneNumber, String otp, String reference) {
        String message = String.format("Your verification code is: %s. Do not share this code with anyone. Valid for 10 minutes.", otp);
        return new SmsRequest(senderId, phoneNumber, message, reference);
    }
}