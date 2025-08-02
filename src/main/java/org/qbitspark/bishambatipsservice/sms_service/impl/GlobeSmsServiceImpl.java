package org.qbitspark.bishambatipsservice.sms_service.impl;

import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.sms_service.GlobeSmsService;
import org.qbitspark.bishambatipsservice.sms_service.payload.SmsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobeSmsServiceImpl implements GlobeSmsService {

    private final GlobeSmsServiceHelper smsHelper;
    @Override
    public void sendOTPSms(String phoneNumber, String otp) {

        SmsResponse response = smsHelper.sendOtpSms(phoneNumber, otp);

        if (!response.isSuccessful()) {
            throw new RuntimeException("SMS delivery failed - check SMS provider response");
        }


    }
}
