package com.insurance_policy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.insurance_policy.service.impl.PolicyServiceImpl;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private InsurancePlanRepository planRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private MemberDocumentRepository memberDocumentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private PolicyServiceImpl policyService;

    private InsurancePlan insurancePlan;
    private Policy policy;
    private PlanRequest planRequest;
    private PolicyEnrollmentRequest enrollmentRequest;
    private MemberDocument memberDocument;

    @BeforeEach
    void setUp() {
        // Create service manually and inject all dependencies including KafkaTemplate
        policyService = new PolicyServiceImpl(planRepository, policyRepository, memberDocumentRepository, null);
        ReflectionTestUtils.setField(policyService, "kafkaTemplate", kafkaTemplate);
        
        insurancePlan = new InsurancePlan();
        insurancePlan.setId(1);
        insurancePlan.setName("Gold Plan");
        insurancePlan.setCoverageAmount(500000.0);
        insurancePlan.setBasePremium(10000.0);
        insurancePlan.setDescription("Comprehensive health coverage");

        policy = new Policy();
        policy.setId(1);
        policy.setUserId(100);
        policy.setAgentId(200);
        policy.setInsurancePlan(insurancePlan);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setPremium(10000.0);
        policy.setRemainingSumInsured(500000.0);
        policy.setPolicyNumber("POL-123456");

        planRequest = new PlanRequest();
        planRequest.setName("Silver Plan");
        planRequest.setCoverageAmount(300000.0);
        planRequest.setBasePremium(5000.0);
        planRequest.setDescription("Basic health coverage");

        enrollmentRequest = new PolicyEnrollmentRequest();
        enrollmentRequest.setUserId(100);
        enrollmentRequest.setAgentId(200);
        enrollmentRequest.setPlanId(1);

        memberDocument = new MemberDocument();
        memberDocument.setId(1);
        memberDocument.setUserId(100);
        memberDocument.setAadhaarNumber("123456789012");
        memberDocument.setPhotoUrl("http://cloudinary.com/photo.jpg");
        memberDocument.setMedicalCheckupDocUrl("http://cloudinary.com/medical.pdf");
    }

    // =============== CREATE PLAN TESTS ===============

    @Test
    void createPlan_success() {
        when(planRepository.save(any(InsurancePlan.class))).thenReturn(insurancePlan);

        PlanResponse response = policyService.createPlan(planRequest);

        assertNotNull(response);
        assertEquals("Gold Plan", response.getName());
        assertEquals(500000.0, response.getCoverageAmount());
        verify(planRepository, times(1)).save(any(InsurancePlan.class));
    }

    @Test
    void createPlan_withAllFields() {
        InsurancePlan savedPlan = new InsurancePlan();
        savedPlan.setId(2);
        savedPlan.setName("Silver Plan");
        savedPlan.setCoverageAmount(300000.0);
        savedPlan.setBasePremium(5000.0);
        savedPlan.setDescription("Basic health coverage");

        when(planRepository.save(any(InsurancePlan.class))).thenReturn(savedPlan);

        PlanResponse response = policyService.createPlan(planRequest);

        assertNotNull(response);
        assertEquals("Silver Plan", response.getName());
        assertEquals(300000.0, response.getCoverageAmount());
        assertEquals(5000.0, response.getBasePremium());
    }

    // =============== GET ALL PLANS TESTS ===============

    @Test
    void getAllPlans_success() {
        InsurancePlan plan2 = new InsurancePlan();
        plan2.setId(2);
        plan2.setName("Platinum Plan");
        plan2.setCoverageAmount(1000000.0);

        when(planRepository.findAll()).thenReturn(Arrays.asList(insurancePlan, plan2));

        List<PlanResponse> responses = policyService.getAllPlans();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Gold Plan", responses.get(0).getName());
        assertEquals("Platinum Plan", responses.get(1).getName());
        verify(planRepository, times(1)).findAll();
    }

    @Test
    void getAllPlans_emptyList() {
        when(planRepository.findAll()).thenReturn(Collections.emptyList());

        List<PlanResponse> responses = policyService.getAllPlans();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(planRepository, times(1)).findAll();
    }

    // =============== GET ALL POLICIES TESTS ===============

    @Test
    void getAllPolicies_success() {
        Policy policy2 = new Policy();
        policy2.setId(2);
        policy2.setUserId(101);
        policy2.setInsurancePlan(insurancePlan);

        when(policyRepository.findAll()).thenReturn(Arrays.asList(policy, policy2));
        when(memberDocumentRepository.findByUserId(anyInt())).thenReturn(Optional.of(memberDocument));

        List<PolicyResponse> responses = policyService.getAllPolicies();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(policyRepository, times(1)).findAll();
    }

    @Test
    void getAllPolicies_emptyList() {
        when(policyRepository.findAll()).thenReturn(Collections.emptyList());

        List<PolicyResponse> responses = policyService.getAllPolicies();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getAllPolicies_withoutMemberDocuments() {
        when(policyRepository.findAll()).thenReturn(Arrays.asList(policy));
        when(memberDocumentRepository.findByUserId(anyInt())).thenReturn(Optional.empty());

        List<PolicyResponse> responses = policyService.getAllPolicies();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertNull(responses.get(0).getMemberDocuments());
    }

    // =============== ENROLL POLICY TESTS ===============

    @Test
    void enrollPolicy_success() {
        when(memberDocumentRepository.existsByUserId(100)).thenReturn(true);
        when(planRepository.findById(1)).thenReturn(Optional.of(insurancePlan));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        PolicyResponse response = policyService.enrollPolicy(enrollmentRequest);

        assertNotNull(response);
        assertEquals(100, response.getUserId());
        assertEquals(200, response.getAgentId());
        assertEquals(500000.0, response.getRemainingSumInsured());
        verify(policyRepository, times(1)).save(any(Policy.class));
        verify(kafkaTemplate, times(1)).send(eq("notification_topic"), any());
    }

    @Test
    void enrollPolicy_documentsNotSubmitted() {
        when(memberDocumentRepository.existsByUserId(100)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.enrollPolicy(enrollmentRequest));

        assertTrue(ex.getMessage().contains("Member documents"));
        verify(policyRepository, never()).save(any(Policy.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void enrollPolicy_planNotFound() {
        when(memberDocumentRepository.existsByUserId(100)).thenReturn(true);
        when(planRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.enrollPolicy(enrollmentRequest));

        assertTrue(ex.getMessage().contains("Plan not found"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    // =============== GET POLICIES BY MEMBER TESTS ===============

    @Test
    void getPoliciesByMember_success() {
        when(policyRepository.findByUserId(100)).thenReturn(Arrays.asList(policy));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        List<PolicyResponse> responses = policyService.getPoliciesByMember(100);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100, responses.get(0).getUserId());
        verify(policyRepository, times(1)).findByUserId(100);
    }

    @Test
    void getPoliciesByMember_noPolicies() {
        when(policyRepository.findByUserId(999)).thenReturn(Collections.emptyList());

        List<PolicyResponse> responses = policyService.getPoliciesByMember(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getPoliciesByMember_multiplePolicies() {
        Policy policy2 = new Policy();
        policy2.setId(2);
        policy2.setUserId(100);
        policy2.setInsurancePlan(insurancePlan);

        when(policyRepository.findByUserId(100)).thenReturn(Arrays.asList(policy, policy2));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        List<PolicyResponse> responses = policyService.getPoliciesByMember(100);

        assertEquals(2, responses.size());
    }

    // =============== GET POLICIES BY AGENT TESTS ===============

    @Test
    void getPoliciesByAgent_success() {
        when(policyRepository.findByAgentId(200)).thenReturn(Arrays.asList(policy));
        when(memberDocumentRepository.findByUserId(anyInt())).thenReturn(Optional.of(memberDocument));

        List<PolicyResponse> responses = policyService.getPoliciesByAgent(200);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(200, responses.get(0).getAgentId());
        verify(policyRepository, times(1)).findByAgentId(200);
    }

    @Test
    void getPoliciesByAgent_noPolicies() {
        when(policyRepository.findByAgentId(999)).thenReturn(Collections.emptyList());

        List<PolicyResponse> responses = policyService.getPoliciesByAgent(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== GET POLICY BY ID TESTS ===============

    @Test
    void getPolicyById_success() {
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        PolicyResponse response = policyService.getPolicyById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        verify(policyRepository, times(1)).findById(1);
    }

    @Test
    void getPolicyById_notFound() {
        when(policyRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.getPolicyById(99));

        assertTrue(ex.getMessage().contains("Policy not found"));
        verify(policyRepository, times(1)).findById(99);
    }

    // =============== IS POLICY ACTIVE TESTS ===============

    @Test
    void isPolicyActive_true() {
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Boolean isActive = policyService.isPolicyActive(1);

        assertTrue(isActive);
        verify(policyRepository, times(1)).findById(1);
    }

    @Test
    void isPolicyActive_false_expired() {
        policy.setStatus(PolicyStatus.EXPIRED);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Boolean isActive = policyService.isPolicyActive(1);

        assertFalse(isActive);
    }

    @Test
    void isPolicyActive_false_cancelled() {
        policy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Boolean isActive = policyService.isPolicyActive(1);

        assertFalse(isActive);
    }

    @Test
    void isPolicyActive_false_beforeStartDate() {
        policy.setStartDate(LocalDate.now().plusDays(10));
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Boolean isActive = policyService.isPolicyActive(1);

        assertFalse(isActive);
    }

    @Test
    void isPolicyActive_false_afterEndDate() {
        policy.setEndDate(LocalDate.now().minusDays(1));
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Boolean isActive = policyService.isPolicyActive(1);

        assertFalse(isActive);
    }

    @Test
    void isPolicyActive_policyNotFound() {
        when(policyRepository.findById(99)).thenReturn(Optional.empty());

        Boolean isActive = policyService.isPolicyActive(99);

        assertFalse(isActive);
    }

    // =============== DEDUCT COVERAGE TESTS ===============

    @Test
    void deductCoverage_success() {
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        PolicyResponse response = policyService.deductCoverage(1, 50000.0);

        assertNotNull(response);
        assertEquals(450000.0, policy.getRemainingSumInsured());
        verify(policyRepository, times(1)).save(policy);
        verify(kafkaTemplate, times(1)).send(eq("notification_topic"), any());
    }

    @Test
    void deductCoverage_insufficientCoverage() {
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.deductCoverage(1, 600000.0));

        assertTrue(ex.getMessage().contains("exceeds remaining sum insured"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void deductCoverage_policyNotFound() {
        when(policyRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.deductCoverage(99, 50000.0));

        assertTrue(ex.getMessage().contains("Policy not found"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void deductCoverage_inactivePolicy() {
        policy.setStatus(PolicyStatus.EXPIRED);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.deductCoverage(1, 50000.0));

        assertTrue(ex.getMessage().contains("inactive policy"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void deductCoverage_nullRemainingSumInsured() {
        policy.setRemainingSumInsured(null);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        PolicyResponse response = policyService.deductCoverage(1, 50000.0);

        assertNotNull(response);
        // Should initialize from plan coverage amount and deduct
        assertEquals(450000.0, policy.getRemainingSumInsured());
    }

    @Test
    void deductCoverage_exactAmount() {
        policy.setRemainingSumInsured(50000.0);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        PolicyResponse response = policyService.deductCoverage(1, 50000.0);

        assertNotNull(response);
        assertEquals(0.0, policy.getRemainingSumInsured());
    }

    // =============== AADHAAR MASKING TEST ===============

    @Test
    void getPolicyById_withMaskedAadhaar() {
        memberDocument.setAadhaarNumber("123456789012");
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        PolicyResponse response = policyService.getPolicyById(1);

        assertNotNull(response);
        assertNotNull(response.getMemberDocuments());
        assertEquals("XXXX-XXXX-9012", response.getMemberDocuments().getAadhaarNumber());
    }

    @Test
    void getPolicyById_withInvalidAadhaar() {
        memberDocument.setAadhaarNumber("123"); // Invalid length
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        PolicyResponse response = policyService.getPolicyById(1);

        assertNotNull(response);
        assertEquals("123", response.getMemberDocuments().getAadhaarNumber()); // Not masked
    }

    @Test
    void getPolicyById_withNullAadhaar() {
        memberDocument.setAadhaarNumber(null);
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(memberDocumentRepository.findByUserId(100)).thenReturn(Optional.of(memberDocument));

        PolicyResponse response = policyService.getPolicyById(1);

        assertNotNull(response);
        assertNull(response.getMemberDocuments().getAadhaarNumber());
    }
}
