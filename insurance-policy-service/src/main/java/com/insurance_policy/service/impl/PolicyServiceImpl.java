package com.insurance_policy.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.insurance_policy.InsurancePolicyServiceApplication;
import com.insurance_policy.dto.MemberDocumentResponse;
import com.insurance_policy.dto.NotificationEvent;
import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.model.InsurancePlan;
import com.insurance_policy.model.MemberDocument;
import com.insurance_policy.model.Policy;
import com.insurance_policy.model.PolicyStatus;
import com.insurance_policy.repository.InsurancePlanRepository;
import com.insurance_policy.repository.MemberDocumentRepository;
import com.insurance_policy.repository.PolicyRepository;
import com.insurance_policy.service.PolicyService;

@Service
public class PolicyServiceImpl implements PolicyService{

	@Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
	
	private final InsurancePlanRepository planRepository;
    private final PolicyRepository policyRepository;
    private final MemberDocumentRepository memberDocumentRepository;

    public PolicyServiceImpl(InsurancePlanRepository planRepository, PolicyRepository policyRepository, 
            MemberDocumentRepository memberDocumentRepository, InsurancePolicyServiceApplication insurancePolicyServiceApplication) {
        this.planRepository = planRepository;
        this.policyRepository = policyRepository;
        this.memberDocumentRepository = memberDocumentRepository;
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
        // Check if member has submitted required documents
        if (!memberDocumentRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Member documents (Aadhaar, Photo, Medical Checkup) are required before enrolling in a policy. Please submit documents first.");
        }
        
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
        // Initialize remaining sum insured with plan's coverage amount
        policy.setRemainingSumInsured(plan.getCoverageAmount());

        Policy savedPolicy = policyRepository.save(policy);
        
        Integer userId = request.getUserId();
        
        NotificationEvent event = new NotificationEvent(
            userId,
            "Policy Enrollment Confirmed",
            "You have successfully enrolled in Plan #" + request.getPlanId() + 
            ". Your Policy Number is " + savedPolicy.getPolicyNumber()
        );
        kafkaTemplate.send("notification_topic", event);
        
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
    
    @Override
    public PolicyResponse getPolicyById(Integer id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
        return mapToPolicyResponse(policy);
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
        response.setCoverageAmount(policy.getInsurancePlan().getCoverageAmount());
        
        // Include member documents if available
        memberDocumentRepository.findByUserId(policy.getUserId())
            .ifPresent(doc -> response.setMemberDocuments(mapToMemberDocumentResponse(doc)));
        
        return response;
    }
    
    @Override
    public PolicyResponse deductCoverage(Integer policyId, Double amount) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));
        
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Cannot deduct coverage from inactive policy");
        }
        
        Double currentRemaining = policy.getRemainingSumInsured();
        
        // If remainingSumInsured is null (for existing policies), initialize with coverage amount
        if (currentRemaining == null) {
            currentRemaining = policy.getInsurancePlan().getCoverageAmount();
        }
        
        if (amount > currentRemaining) {
            throw new RuntimeException("Deduction amount (" + amount + ") exceeds remaining sum insured (" + currentRemaining + ")");
        }
        
        Double newRemaining = currentRemaining - amount;
        policy.setRemainingSumInsured(newRemaining);
        
        Policy savedPolicy = policyRepository.save(policy);
        
        // Send notification about coverage deduction
        NotificationEvent event = new NotificationEvent(
            policy.getUserId(),
            "Policy Coverage Update",
            "₹" + amount + " has been deducted from your policy coverage. Remaining sum insured: ₹" + newRemaining
        );
        kafkaTemplate.send("notification_topic", event);
        
        return mapToPolicyResponse(savedPolicy);
    }
    
    private MemberDocumentResponse mapToMemberDocumentResponse(MemberDocument document) {
        return MemberDocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .aadhaarNumber(maskAadhaarNumber(document.getAadhaarNumber()))
                .photoUrl(document.getPhotoUrl())
                .medicalCheckupDocUrl(document.getMedicalCheckupDocUrl())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    
    // Mask Aadhaar for security: XXXX-XXXX-1234
    private String maskAadhaarNumber(String aadhaarNumber) {
        if (aadhaarNumber == null || aadhaarNumber.length() != 12) {
            return aadhaarNumber;
        }
        return "XXXX-XXXX-" + aadhaarNumber.substring(8);
    }
}
