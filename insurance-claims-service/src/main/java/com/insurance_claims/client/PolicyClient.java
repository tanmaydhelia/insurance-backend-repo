package com.insurance_claims.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.insurance_claims.dto.PolicyDTO;

@FeignClient(name = "insurance-policy-service")
public interface PolicyClient {

    @GetMapping("/api/policies/{id}")
    PolicyDTO getPolicyById(@PathVariable Integer id);

    @GetMapping("/api/policies/member/{userId}")
    List<PolicyDTO> getPoliciesByMember(@PathVariable Integer userId);
}