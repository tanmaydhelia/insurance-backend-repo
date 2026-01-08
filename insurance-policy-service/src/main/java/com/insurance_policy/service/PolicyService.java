package com.insurance_policy.service;

import java.util.List;

import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.dto.RenewalConfirmRequest;
import com.insurance_policy.dto.RenewalOrderResponse;

public interface PolicyService {
	PlanResponse createPlan(PlanRequest request);
    List<PlanResponse> getAllPlans();
    List<PolicyResponse> getAllPolicies();
    PolicyResponse enrollPolicy(PolicyEnrollmentRequest request);
    List<PolicyResponse> getPoliciesByMember(Integer userId);
    List<PolicyResponse> getPoliciesByAgent(Integer agentId);
    Boolean isPolicyActive(Integer policyId);
    PolicyResponse getPolicyById(Integer id);
    PolicyResponse getPolicyByPolicyNumber(String policyNumber);
    PolicyResponse deductCoverage(Integer policyId, Double amount);
    
    // Renewal methods
    List<PolicyResponse> getExpiringPolicies(Integer withinDays);
    List<PolicyResponse> getExpiringPoliciesByAgent(Integer agentId, Integer withinDays);
    void sendRenewalReminder(Integer policyId, Integer agentId);
    RenewalOrderResponse initiateRenewal(Integer policyId);
    PolicyResponse confirmRenewal(Integer policyId, RenewalConfirmRequest request);
}
