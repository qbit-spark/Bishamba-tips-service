package org.qbitspark.bishambatipsservice.sms_service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qbitspark.bishambatipsservice.globe_api_client.HttpClientService;
import org.qbitspark.bishambatipsservice.sms_service.payload.SmsRequest;
import org.qbitspark.bishambatipsservice.sms_service.payload.SmsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobeSmsServiceHelper {

    @Value("${api.sms-username}")
    private String username;

    @Value("${api.sms-password}")
    private String password;

    @Value("${api.sms-url}")
    private String url;

    @Value("${api.sms-sender-id}")
    private String senderId;

    private final HttpClientService httpClientService;

    public SmsResponse sendOtpSms(String phoneNumber, String otp) {
        try {
            log.info("Sending OTP SMS to: {}", phoneNumber);

            String reference = generateReference();
            SmsRequest request = SmsRequest.createOtpSms(senderId, phoneNumber, otp, reference);

            String endpoint = url+"text/single";

            SmsResponse response = httpClientService.postWithBasicAuth(
                    endpoint,
                    request,
                    username,
                    password,
                    SmsResponse.class
            );

            if (response.isSuccessful()) {
                log.info("SMS sent successfully to: {} with reference: {}", phoneNumber, reference);
            } else {
                log.warn("SMS sending failed for: {}", phoneNumber);
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    public SmsResponse sendPlainTextSms(String phoneNumber, String message) {
        try {
            log.info("Sending plain SMS to: {}", phoneNumber);

            String reference = generateReference();
            SmsRequest request = new SmsRequest(senderId, phoneNumber, message, reference);
            String endpoint = url+"text/single";

            return httpClientService.postWithBasicAuth(
                    endpoint,
                    request,
                    username,
                    password,
                    SmsResponse.class
            );

        } catch (Exception e) {
            log.error("Failed to send plain SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    private String generateReference() {
        return "OTP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}