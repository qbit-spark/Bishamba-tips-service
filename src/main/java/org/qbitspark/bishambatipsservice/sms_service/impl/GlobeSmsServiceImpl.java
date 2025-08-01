package org.qbitspark.bishambatipsservice.sms_service.impl;

import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.sms_service.GlobeSmsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobeSmsServiceImpl implements GlobeSmsService {
    @Override
    public void sendOTPSms(String phoneNumber, String otp) {

    }
}
