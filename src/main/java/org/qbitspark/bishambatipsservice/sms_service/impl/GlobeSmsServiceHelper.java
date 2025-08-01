package org.qbitspark.bishambatipsservice.sms_service.impl;

import lombok.RequiredArgsConstructor;
import org.qbitspark.bishambatipsservice.globe_api_client.HttpClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobeSmsServiceHelper {

    /***
     *  In this class we manage all SMS templates that will be used!!
     */

    @Value( "${api.sms-username}")
    private String username;
    @Value( "${api.sms-password}")
    private String password;
    @Value( "${api.sms-url}")
    private String url;

    private final HttpClientService httpClientService;
    public void sendPlainTextSms(){



    }


    public void sendWaterMarkSms(){


    }
}
