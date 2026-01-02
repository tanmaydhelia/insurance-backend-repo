package com.insurance_policy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.service.PolicyService;

@RestController
@RequestMapping("/policy")
public class PolicyController {
	private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }
    
    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(@RequestBody PlanRequest request) {
        return new ResponseEntity<>(policyService.createPlan(request), HttpStatus.CREATED);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(policyService.getAllPlans());
    }

    @PostMapping("/policies/enroll")
    public ResponseEntity<PolicyResponse> enrollPolicy(@RequestBody PolicyEnrollmentRequest request) {
        return new ResponseEntity<>(policyService.enrollPolicy(request), HttpStatus.CREATED);
    }

    @GetMapping("/policies/member/{userId}")
    public ResponseEntity<List<PolicyResponse>> getMemberPolicies(@PathVariable Integer userId) {
        return ResponseEntity.ok(policyService.getPoliciesByMember(userId));
    }

    @GetMapping("/policies/agent/{agentId}")
    public ResponseEntity<List<PolicyResponse>> getAgentPolicies(@PathVariable Integer agentId) {
        return ResponseEntity.ok(policyService.getPoliciesByAgent(agentId));
    }
    
    @GetMapping("/policies/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Integer id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }
}
