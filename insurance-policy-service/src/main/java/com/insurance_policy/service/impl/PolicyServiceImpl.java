package com.insurance_policy.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.insurance_policy.InsurancePolicyServiceApplication;
import com.insurance_policy.dto.MemberDocumentResponse;
import com.insurance_policy.dto.NotificationEvent;
import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.dto.RenewalConfirmRequest;
import com.insurance_policy.dto.RenewalOrderResponse;
import com.insurance_policy.exception.InvalidPolicyStateException;
import com.insurance_policy.exception.PlanNotFoundException;
import com.insurance_policy.exception.PolicyNotFoundException;
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
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(this::mapToPolicyResponse)
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
    
    @Override
    public PolicyResponse getPolicyByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null) {
            throw new PolicyNotFoundException("Policy not found with policy number: " + policyNumber);
        }
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
        
        // Calculate days remaining before expiry
        if (policy.getEndDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate());
            response.setDaysRemaining(daysRemaining);
            // Policy is renewable if active and has 90 or fewer days remaining
            response.setRenewable(policy.getStatus() == PolicyStatus.ACTIVE && daysRemaining <= 90);
        } else {
            response.setDaysRemaining(null);
            response.setRenewable(false);
        }
        
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
    
    // ==================== RENEWAL METHODS ====================
    
    @Override
    public void sendRenewalReminder(Integer policyId, Integer agentId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));
        
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidPolicyStateException("Can only send renewal reminders for active policies");
        }
        
        InsurancePlan plan = policy.getInsurancePlan();
        if (plan == null) {
            throw new PlanNotFoundException("Plan not found for policy: " + policyId);
        }
        
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate());
        
        // Update renewal requested timestamp
        policy.setRenewalRequestedAt(LocalDateTime.now());
        policyRepository.save(policy);
        
        // Send HTML email notification
        String htmlContent = buildRenewalReminderEmail(policy, plan, daysRemaining);
        NotificationEvent event = new NotificationEvent(
            policy.getUserId(),
            "Policy Renewal Reminder - RestO'Sure",
            htmlContent
        );
        kafkaTemplate.send("notification_topic", event);
    }
    
    @Override
    public RenewalOrderResponse initiateRenewal(Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));
        
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidPolicyStateException("Can only renew active policies");
        }
        
        InsurancePlan plan = policy.getInsurancePlan();
        if (plan == null) {
            throw new PlanNotFoundException("Plan not found for policy: " + policyId);
        }
        
        // Mark renewal attempt
        policy.setLastRenewalAttemptAt(LocalDateTime.now());
        policy.setLastRenewalStatus("PENDING");
        policyRepository.save(policy);
        
        // Generate order ID (in real scenario, call Razorpay/billing service)
        String orderId = "order_" + System.currentTimeMillis();
        
        return RenewalOrderResponse.builder()
                .orderId(orderId)
                .amount(plan.getBasePremium())
                .currency("INR")
                .policyId(policyId)
                .build();
    }
    
    @Override
    @Transactional
    public PolicyResponse confirmRenewal(Integer policyId, RenewalConfirmRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));
        
        InsurancePlan plan = policy.getInsurancePlan();
        if (plan == null) {
            throw new PlanNotFoundException("Plan not found for policy: " + policyId);
        }
        
        if (request.getSuccess()) {
            // Renewal successful - extend policy by 1 year
            LocalDate newStartDate = policy.getEndDate().plusDays(1);
            LocalDate newEndDate = newStartDate.plusYears(1);
            
            policy.setStartDate(newStartDate);
            policy.setEndDate(newEndDate);
            policy.setRemainingSumInsured(plan.getCoverageAmount()); // Reset coverage
            policy.setLastRenewalStatus("SUCCESS");
            policy.setStatus(PolicyStatus.ACTIVE);
            
            Policy savedPolicy = policyRepository.save(policy);
            
            // Send renewal success notification
            String htmlContent = buildRenewalSuccessEmail(policy, plan, newEndDate);
            NotificationEvent event = new NotificationEvent(
                policy.getUserId(),
                "Policy Renewed Successfully - RestO'Sure",
                htmlContent
            );
            kafkaTemplate.send("notification_topic", event);
            
            return mapToPolicyResponse(savedPolicy);
        } else {
            // Renewal failed - mark policy as expired
            policy.setLastRenewalStatus("FAILED");
            policy.setStatus(PolicyStatus.EXPIRED);
            
            Policy savedPolicy = policyRepository.save(policy);
            
            // Send renewal failure notification
            String htmlContent = buildRenewalFailedEmail(policy, plan);
            NotificationEvent event = new NotificationEvent(
                policy.getUserId(),
                "Policy Renewal Failed - RestO'Sure",
                htmlContent
            );
            kafkaTemplate.send("notification_topic", event);
            
            return mapToPolicyResponse(savedPolicy);
        }
    }
    
    @Override
    public List<PolicyResponse> getExpiringPolicies(Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return policyRepository.findByEndDateBetweenAndStatus(today, endDate, PolicyStatus.ACTIVE)
                .stream()
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PolicyResponse> getExpiringPoliciesByAgent(Integer agentId, Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return policyRepository.findByEndDateBetweenAndStatus(today, endDate, PolicyStatus.ACTIVE)
                .stream()
                .filter(policy -> policy.getAgentId() != null && policy.getAgentId().equals(agentId))
                .map(this::mapToPolicyResponse)
                .collect(Collectors.toList());
    }
    
    // ==================== RENEWAL EMAIL TEMPLATES ====================
    
    private String buildRenewalReminderEmail(Policy policy, InsurancePlan plan, long daysRemaining) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #b8d4e8; margin: 5px 0 0 0; font-size: 14px; }
                    .content { padding: 40px 30px; }
                    .alert-box { background: linear-gradient(135deg, #ff6b35 0%%, #f7931e 100%%); border-radius: 10px; padding: 25px; text-align: center; margin-bottom: 30px; }
                    .alert-box h2 { color: #ffffff; margin: 0 0 10px 0; font-size: 24px; }
                    .alert-box .days { color: #ffffff; font-size: 48px; font-weight: bold; }
                    .alert-box p { color: #fff3e0; margin: 10px 0 0 0; }
                    .policy-details { background-color: #f8f9fa; border-radius: 10px; padding: 25px; margin-bottom: 30px; }
                    .policy-details h3 { color: #1e3a5f; margin: 0 0 15px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e9ecef; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { color: #6c757d; }
                    .detail-value { color: #1e3a5f; font-weight: 600; }
                    .renew-btn { display: block; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; text-align: center; font-size: 18px; font-weight: bold; margin: 20px 0; }
                    .footer { background-color: #1e3a5f; padding: 25px; text-align: center; }
                    .footer p { color: #b8d4e8; margin: 5px 0; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>RestO'Sure</h1>
                        <p>Smart Health Insurance</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <h2>⏰ Policy Expiring Soon!</h2>
                            <div class="days">%d</div>
                            <p>days remaining until expiry</p>
                        </div>
                        <div class="policy-details">
                            <h3>📋 Policy Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Plan Name</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Policy ID</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Expiry Date</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Coverage Amount</span>
                                <span class="detail-value">₹%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Renewal Premium</span>
                                <span class="detail-value">₹%s</span>
                            </div>
                        </div>
                        <p style="text-align: center; color: #6c757d;">Don't let your coverage lapse! Renew now to continue enjoying uninterrupted health protection.</p>
                        <a href="#" class="renew-btn">🔄 Renew My Policy</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 RestO'Sure. All rights reserved.</p>
                        <p>This is an automated reminder. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                daysRemaining,
                plan.getName(),
                policy.getId(),
                policy.getEndDate().toString(),
                String.format("%,.2f", plan.getCoverageAmount()),
                String.format("%,.2f", plan.getBasePremium())
            );
    }
    
    private String buildRenewalSuccessEmail(Policy policy, InsurancePlan plan, LocalDate newEndDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #b8d4e8; margin: 5px 0 0 0; font-size: 14px; }
                    .content { padding: 40px 30px; }
                    .success-box { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); border-radius: 10px; padding: 30px; text-align: center; margin-bottom: 30px; }
                    .success-box h2 { color: #ffffff; margin: 0; font-size: 28px; }
                    .success-box p { color: #e8f5e9; margin: 10px 0 0 0; }
                    .checkmark { font-size: 60px; margin-bottom: 15px; }
                    .policy-details { background-color: #f8f9fa; border-radius: 10px; padding: 25px; margin-bottom: 30px; }
                    .policy-details h3 { color: #1e3a5f; margin: 0 0 15px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e9ecef; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { color: #6c757d; }
                    .detail-value { color: #1e3a5f; font-weight: 600; }
                    .footer { background-color: #1e3a5f; padding: 25px; text-align: center; }
                    .footer p { color: #b8d4e8; margin: 5px 0; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>RestO'Sure</h1>
                        <p>Smart Health Insurance</p>
                    </div>
                    <div class="content">
                        <div class="success-box">
                            <div class="checkmark">✅</div>
                            <h2>Policy Renewed Successfully!</h2>
                            <p>Your health coverage continues uninterrupted</p>
                        </div>
                        <div class="policy-details">
                            <h3>📋 Updated Policy Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Plan Name</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Policy ID</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">New Valid Until</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Coverage Amount</span>
                                <span class="detail-value">₹%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Amount Paid</span>
                                <span class="detail-value">₹%s</span>
                            </div>
                        </div>
                        <p style="text-align: center; color: #28a745; font-weight: bold;">🎉 Thank you for continuing with RestO'Sure!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 RestO'Sure. All rights reserved.</p>
                        <p>For any queries, contact support@restosure.com</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                plan.getName(),
                policy.getId(),
                newEndDate.toString(),
                String.format("%,.2f", plan.getCoverageAmount()),
                String.format("%,.2f", plan.getBasePremium())
            );
    }
    
    private String buildRenewalFailedEmail(Policy policy, InsurancePlan plan) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #b8d4e8; margin: 5px 0 0 0; font-size: 14px; }
                    .content { padding: 40px 30px; }
                    .error-box { background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); border-radius: 10px; padding: 30px; text-align: center; margin-bottom: 30px; }
                    .error-box h2 { color: #ffffff; margin: 0; font-size: 24px; }
                    .error-box p { color: #f8d7da; margin: 10px 0 0 0; }
                    .warning-icon { font-size: 60px; margin-bottom: 15px; }
                    .policy-details { background-color: #fff3cd; border-radius: 10px; padding: 25px; margin-bottom: 30px; border: 1px solid #ffc107; }
                    .policy-details h3 { color: #856404; margin: 0 0 15px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #ffe69c; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { color: #856404; }
                    .detail-value { color: #664d03; font-weight: 600; }
                    .retry-btn { display: block; background: linear-gradient(135deg, #007bff 0%%, #0056b3 100%%); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; text-align: center; font-size: 16px; font-weight: bold; margin: 20px 0; }
                    .footer { background-color: #1e3a5f; padding: 25px; text-align: center; }
                    .footer p { color: #b8d4e8; margin: 5px 0; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>RestO'Sure</h1>
                        <p>Smart Health Insurance</p>
                    </div>
                    <div class="content">
                        <div class="error-box">
                            <div class="warning-icon">⚠️</div>
                            <h2>Policy Renewal Failed</h2>
                            <p>We couldn't process your renewal payment</p>
                        </div>
                        <div class="policy-details">
                            <h3>⚠️ Policy Status: EXPIRED</h3>
                            <div class="detail-row">
                                <span class="detail-label">Plan Name</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Policy ID</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Status</span>
                                <span class="detail-value" style="color: #dc3545;">EXPIRED</span>
                            </div>
                        </div>
                        <p style="text-align: center; color: #6c757d;">Your policy coverage has ended. Please contact our support team or try renewing again.</p>
                        <a href="#" class="retry-btn">📞 Contact Support</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 RestO'Sure. All rights reserved.</p>
                        <p>For immediate assistance, call 1800-XXX-XXXX</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                plan.getName(),
                policy.getId()
            );
    }
}
