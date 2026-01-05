package com.insurance_billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Configuration
public class RazorpayConfig {
    private String keyId = "rzp_test_S0HBlUeoLspOGH";

    private String keySecret = "ITP0f47naRIM5M2kMduo4fW3";

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
