package org.qbitspark.bishambatipsservice.sms_service;

public interface GlobeSmsService {
    void sendOTPSms(String phoneNumber, String otp);
    void sendSimpleSms(String phoneNumber, String message);
}
