package com.insurance_policy.service;

import java.util.List;

import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;

public interface PolicyService {
	PlanResponse createPlan(PlanRequest request);
    List<PlanResponse> getAllPlans();
    PolicyResponse enrollPolicy(PolicyEnrollmentRequest request);
    List<PolicyResponse> getPoliciesByMember(Integer userId);
    List<PolicyResponse> getPoliciesByAgent(Integer agentId);
    Boolean isPolicyActive(Integer policyId);
    PolicyResponse getPolicyById(Integer id);
}
