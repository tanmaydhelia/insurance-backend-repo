package com.insurance_policy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.insurance_policy.InsurancePolicyServiceApplication;
import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.model.InsurancePlan;
import com.insurance_policy.model.Policy;
import com.insurance_policy.model.PolicyStatus;
import com.insurance_policy.repository.InsurancePlanRepository;
import com.insurance_policy.repository.PolicyRepository;
import com.insurance_policy.service.PolicyService;

@Service
public class PolicyServiceImpl implements PolicyService{

	
	private final InsurancePlanRepository planRepository;
    private final PolicyRepository policyRepository;

    public PolicyServiceImpl(InsurancePlanRepository planRepository, PolicyRepository policyRepository, InsurancePolicyServiceApplication insurancePolicyServiceApplication) {
        this.planRepository = planRepository;
        this.policyRepository = policyRepository;
    }
    
    @Override
    public PlanResponse createPlan(PlanRequest request) {
        InsurancePlan plan = new InsurancePlan();
        BeanUtils.copyProperties(request, plan);
        InsurancePlan savedPlan = planRepository.save(plan);
        return mapToPlanResponse(savedPlan);
    }

    @Override
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PolicyResponse enrollPolicy(PolicyEnrollmentRequest request) {
        InsurancePlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Policy policy = new Policy();
        policy.setUserId(request.getUserId());
        policy.setAgentId(request.getAgentId());
        policy.setInsurancePlan(plan);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setPremium(plan.getBasePremium());

        Policy savedPolicy = policyRepository.save(policy);
        return mapToPolicyResponse(savedPolicy);
    }
    
    @Override
    public Boolean isPolicyActive(Integer policyId) {
        return policyRepository.findById(policyId)
                .map(policy -> 
                    policy.getStatus() == PolicyStatus.ACTIVE && 
                    !LocalDate.now().isBefore(policy.getStartDate()) &&
                    !LocalDate.now().isAfter(policy.getEndDate())
                )
                .orElse(false);
    }

    @Override
    public List<PolicyResponse> getPoliciesByMember(Integer userId) {
        return policyRepository.findByUserId(userId).stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyResponse> getPoliciesByAgent(Integer agentId) {
        return policyRepository.findByAgentId(agentId).stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }

    private PlanResponse mapToPlanResponse(InsurancePlan plan) {
        PlanResponse response = new PlanResponse();
        BeanUtils.copyProperties(plan, response);
        return response;
    }

    private PolicyResponse mapToPolicyResponse(Policy policy) {
        PolicyResponse response = new PolicyResponse();
        BeanUtils.copyProperties(policy, response);
        response.setPlan(mapToPlanResponse(policy.getInsurancePlan()));
        return response;
    }
}
