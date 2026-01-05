package com.insurance_notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.insurance_notification.dto.UserDTO;

@FeignClient(name = "INSURANCE-IDENTITY-SERVICE")
public interface AuthClient {
    @GetMapping("/auth/user/{id}")
    UserDTO getUserById(@PathVariable Integer id);
}