package com.insurance_policy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance_policy.dto.PlanRequest;
import com.insurance_policy.dto.PlanResponse;
import com.insurance_policy.dto.PolicyEnrollmentRequest;
import com.insurance_policy.dto.PolicyResponse;
import com.insurance_policy.model.PolicyStatus;
import com.insurance_policy.service.PolicyService;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    private PlanRequest planRequest;
    private PlanResponse planResponse;
    private PolicyEnrollmentRequest enrollmentRequest;
    private PolicyResponse policyResponse;

    @BeforeEach
    void setUp() {
        planRequest = new PlanRequest();
        planRequest.setName("Gold Plan");
        planRequest.setCoverageAmount(500000.0);
        planRequest.setBasePremium(10000.0);

        planResponse = new PlanResponse();
        planResponse.setId(1);
        planResponse.setName("Gold Plan");
        planResponse.setCoverageAmount(500000.0);

        enrollmentRequest = new PolicyEnrollmentRequest();
        enrollmentRequest.setUserId(100);
        enrollmentRequest.setAgentId(200);
        enrollmentRequest.setPlanId(1);

        policyResponse = new PolicyResponse();
        policyResponse.setId(1);
        policyResponse.setUserId(100);
        policyResponse.setAgentId(200);
        policyResponse.setStatus(PolicyStatus.ACTIVE);
        policyResponse.setStartDate(LocalDate.now());
        policyResponse.setEndDate(LocalDate.now().plusYears(1));
    }

    @Test
    void createPlan_success() throws Exception {
        when(policyService.createPlan(any(PlanRequest.class))).thenReturn(planResponse);

        mockMvc.perform(post("/policy/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gold Plan"));

        verify(policyService, times(1)).createPlan(any(PlanRequest.class));
    }

    @Test
    void getAllPlans_success() throws Exception {
        when(policyService.getAllPlans()).thenReturn(Arrays.asList(planResponse));

        mockMvc.perform(get("/policy/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gold Plan"));

        verify(policyService, times(1)).getAllPlans();
    }

    @Test
    void getAllPolicies_success() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(Arrays.asList(policyResponse));

        mockMvc.perform(get("/policy/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100));

        verify(policyService, times(1)).getAllPolicies();
    }

    @Test
    void enrollPolicy_success() throws Exception {
        when(policyService.enrollPolicy(any(PolicyEnrollmentRequest.class))).thenReturn(policyResponse);

        mockMvc.perform(post("/policy/policies/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(100));

        verify(policyService, times(1)).enrollPolicy(any(PolicyEnrollmentRequest.class));
    }

    @Test
    void getMemberPolicies_success() throws Exception {
        when(policyService.getPoliciesByMember(anyInt())).thenReturn(Arrays.asList(policyResponse));

        mockMvc.perform(get("/policy/policies/member/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100));

        verify(policyService, times(1)).getPoliciesByMember(100);
    }

    @Test
    void getAgentPolicies_success() throws Exception {
        when(policyService.getPoliciesByAgent(anyInt())).thenReturn(Arrays.asList(policyResponse));

        mockMvc.perform(get("/policy/policies/agent/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].agentId").value(200));

        verify(policyService, times(1)).getPoliciesByAgent(200);
    }

    @Test
    void getPolicyById_success() throws Exception {
        when(policyService.getPolicyById(anyInt())).thenReturn(policyResponse);

        mockMvc.perform(get("/policy/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(policyService, times(1)).getPolicyById(1);
    }

    @Test
    void deductCoverage_success() throws Exception {
        policyResponse.setRemainingSumInsured(450000.0);
        when(policyService.deductCoverage(anyInt(), any(Double.class))).thenReturn(policyResponse);

        mockMvc.perform(put("/policy/policies/1/deduct-coverage")
                .param("amount", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingSumInsured").value(450000.0));

        verify(policyService, times(1)).deductCoverage(anyInt(), any(Double.class));
    }
}
