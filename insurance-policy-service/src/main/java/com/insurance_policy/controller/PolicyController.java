package com.insurance_policy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.dto.RenewalConfirmRequest;
import com.insurance_policy.dto.RenewalOrderResponse;
import com.insurance_policy.dto.RenewalRequest;
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

    @GetMapping("/policies")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
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
    
    /**
     * Get policy by policy number (for hospitals/providers)
     */
    @GetMapping("/policies/number/{policyNumber}")
    public ResponseEntity<PolicyResponse> getPolicyByPolicyNumber(@PathVariable String policyNumber) {
        return ResponseEntity.ok(policyService.getPolicyByPolicyNumber(policyNumber));
    }
    
    @PutMapping("/policies/{id}/deduct-coverage")
    public ResponseEntity<PolicyResponse> deductCoverage(
            @PathVariable Integer id, 
            @RequestParam Double amount) {
        return ResponseEntity.ok(policyService.deductCoverage(id, amount));
    }
    
    // ==================== RENEWAL ENDPOINTS ====================
    
    /**
     * Agent sends renewal reminder email to user
     */
    @PostMapping("/policies/{id}/renewal-reminder")
    public ResponseEntity<Void> sendRenewalReminder(
            @PathVariable Integer id,
            @RequestBody RenewalRequest request) {
        policyService.sendRenewalReminder(id, request.getAgentId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * User initiates renewal - returns payment order details
     */
    @PostMapping("/policies/{id}/renew")
    public ResponseEntity<RenewalOrderResponse> initiateRenewal(@PathVariable Integer id) {
        return ResponseEntity.ok(policyService.initiateRenewal(id));
    }
    
    /**
     * User confirms renewal after payment
     */
    @PostMapping("/policies/{id}/renew/confirm")
    public ResponseEntity<PolicyResponse> confirmRenewal(
            @PathVariable Integer id,
            @RequestBody RenewalConfirmRequest request) {
        return ResponseEntity.ok(policyService.confirmRenewal(id, request));
    }
    
    /**
     * Get policies expiring within specified days (for agent dashboard)
     */
    @GetMapping("/policies/expiring")
    public ResponseEntity<List<PolicyResponse>> getExpiringPolicies(
            @RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(policyService.getExpiringPolicies(days));
    }
    
    /**
     * Get expiring policies for a specific agent
     */
    @GetMapping("/policies/expiring/agent/{agentId}")
    public ResponseEntity<List<PolicyResponse>> getExpiringPoliciesByAgent(
            @PathVariable Integer agentId,
            @RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(policyService.getExpiringPoliciesByAgent(agentId, days));
    }
}
