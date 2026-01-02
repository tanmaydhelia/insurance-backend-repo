package com.insurance_claims.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.insurance_claims.dto.PolicyDTO;

@FeignClient(name = "INSURANCE-POLICY-SERVICE")
public interface PolicyClient {

    @GetMapping("/policy/policies/{id}")
    PolicyDTO getPolicyById(@PathVariable Integer id);

    @GetMapping("/policy/policies/member/{userId}")
    List<PolicyDTO> getPoliciesByMember(@PathVariable Integer userId);
}