package com.insurance_policy.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
    
    @Value("${cloudinary.cloud-name:dhsarmq6x}")
    private String cloudName;

    @Value("${cloudinary.api-key:498351658865896}")
    private String apiKey;

    @Value("${cloudinary.api-secret:ThbMrSsT9ED1Qpm14vlkgtv5JC0}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}
