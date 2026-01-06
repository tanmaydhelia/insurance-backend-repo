package com.insurance_claims.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.insurance_claims.client.PolicyClient;
import com.insurance_claims.dto.ClaimRequest;
import com.insurance_claims.dto.ClaimResponse;
import com.insurance_claims.dto.ClaimStatusDTO;
import com.insurance_claims.dto.PolicyDTO;
import com.insurance_claims.model.Claim;
import com.insurance_claims.model.ClaimStatus;
import com.insurance_claims.model.SubmissionSource;
import com.insurance_claims.repository.ClaimRepository;
import com.insurance_claims.service.impl.ClaimServiceImpl;
import com.insurance_claims.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyClient policyClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Claim claim;
    private ClaimRequest claimRequest;
    private PolicyDTO policyDTO;
    private ClaimStatusDTO statusDTO;
    private String validToken;

    @BeforeEach
    void setUp() {
        claim = Claim.builder()
                .id(1)
                .policyId(100)
                .hospitalId(500)
                .diagnosis("Fever and cold")
                .claimAmount(25000.0)
                .status(ClaimStatus.SUBMITTED)
                .submissionSource(SubmissionSource.MEMBER)
                .documentUrl("http://docs.example.com/claim1.pdf")
                .date(LocalDate.now())
                .build();

        claimRequest = new ClaimRequest();
        claimRequest.setPolicyId(100);
        claimRequest.setDiagnosis("Fever and cold");
        claimRequest.setClaimAmount(25000.0);
        claimRequest.setSubmissionSource(SubmissionSource.MEMBER);
        claimRequest.setDocumentUrl("http://docs.example.com/claim1.pdf");

        policyDTO = new PolicyDTO();
        policyDTO.setId(100);
        policyDTO.setStatus("ACTIVE");
        policyDTO.setUserId(200);
        policyDTO.setRemainingSumInsured(500000.0);
        policyDTO.setCoverageAmount(500000.0);

        statusDTO = new ClaimStatusDTO();
        statusDTO.setStatus(ClaimStatus.IN_REVIEW);

        validToken = "valid.jwt.token";
    }

    // =============== SUBMIT CLAIM TESTS ===============

    @Test
    void submitClaim_success_memberSubmission() {
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        ClaimResponse response = claimService.submitClaim(claimRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(100, response.getPolicyId());
        assertEquals("Fever and cold", response.getDiagnosis());
        assertEquals(ClaimStatus.SUBMITTED, response.getStatus());
        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(kafkaTemplate, times(1)).send(eq("notification_topic"), any());
    }

    @Test
    void submitClaim_success_providerSubmission() {
        claimRequest.setSubmissionSource(SubmissionSource.PROVIDER);
        claimRequest.setHospitalId(500);
        claim.setSubmissionSource(SubmissionSource.PROVIDER);
        claim.setHospitalId(500);

        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        ClaimResponse response = claimService.submitClaim(claimRequest);

        assertNotNull(response);
        assertEquals(500, response.getHospitalId());
        assertEquals(SubmissionSource.PROVIDER, response.getSubmissionSource());
    }

    @Test
    void submitClaim_providerWithoutHospitalId() {
        claimRequest.setSubmissionSource(SubmissionSource.PROVIDER);
        claimRequest.setHospitalId(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.submitClaim(claimRequest));

        assertTrue(ex.getMessage().contains("Hospital ID is required"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void submitClaim_policyNotFound() {
        when(policyClient.getPolicyById(100)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.submitClaim(claimRequest));

        assertTrue(ex.getMessage().contains("Policy not found"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void submitClaim_policyNotActive() {
        policyDTO.setStatus("EXPIRED");
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.submitClaim(claimRequest));

        assertTrue(ex.getMessage().contains("Policy is not ACTIVE"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    // =============== GET CLAIM BY ID TESTS ===============

    @Test
    void getClaimById_success() {
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        ClaimResponse response = claimService.getClaimById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(100, response.getPolicyId());
    }

    @Test
    void getClaimById_notFound() {
        when(claimRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.getClaimById(99));

        assertTrue(ex.getMessage().contains("Claim not found"));
    }

    // =============== GET CLAIMS BY MEMBER TESTS ===============

    @Test
    void getClaimsByMember_success() {
        when(policyClient.getPoliciesByMember(200)).thenReturn(Arrays.asList(policyDTO));
        when(claimRepository.findByPolicyIdIn(anyList())).thenReturn(Arrays.asList(claim));

        List<ClaimResponse> responses = claimService.getClaimsByMember(200);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100, responses.get(0).getPolicyId());
    }

    @Test
    void getClaimsByMember_noPolicies() {
        when(policyClient.getPoliciesByMember(999)).thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getClaimsByMember(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(claimRepository, never()).findByPolicyIdIn(anyList());
    }

    @Test
    void getClaimsByMember_multiplePolicies() {
        PolicyDTO policy2 = new PolicyDTO();
        policy2.setId(101);

        Claim claim2 = Claim.builder()
                .id(2)
                .policyId(101)
                .diagnosis("Back pain")
                .claimAmount(15000.0)
                .status(ClaimStatus.APPROVED)
                .build();

        when(policyClient.getPoliciesByMember(200)).thenReturn(Arrays.asList(policyDTO, policy2));
        when(claimRepository.findByPolicyIdIn(anyList())).thenReturn(Arrays.asList(claim, claim2));

        List<ClaimResponse> responses = claimService.getClaimsByMember(200);

        assertEquals(2, responses.size());
    }

    // =============== GET CLAIMS BY PROVIDER TESTS ===============

    @Test
    void getClaimsByProvider_success() {
        when(claimRepository.findByHospitalId(500)).thenReturn(Arrays.asList(claim));

        List<ClaimResponse> responses = claimService.getClaimsByProvider(500);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getClaimsByProvider_noClaims() {
        when(claimRepository.findByHospitalId(999)).thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getClaimsByProvider(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== GET OPEN CLAIMS TESTS ===============

    @Test
    void getOpenClaims_success() {
        when(claimRepository.findByStatusIn(anyList())).thenReturn(Arrays.asList(claim));

        List<ClaimResponse> responses = claimService.getOpenClaims();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ClaimStatus.SUBMITTED, responses.get(0).getStatus());
    }

    @Test
    void getOpenClaims_empty() {
        when(claimRepository.findByStatusIn(anyList())).thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getOpenClaims();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== GET ALL CLAIMS TESTS ===============

    @Test
    void getAllClaims_success() {
        Claim claim2 = Claim.builder()
                .id(2)
                .policyId(101)
                .status(ClaimStatus.APPROVED)
                .build();

        when(claimRepository.findAll()).thenReturn(Arrays.asList(claim, claim2));

        List<ClaimResponse> responses = claimService.getAllClaims();

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void getAllClaims_empty() {
        when(claimRepository.findAll()).thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getAllClaims();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== UPDATE CLAIM STATUS TESTS ===============

    @Test
    void updateClaimStatus_toInReview() {
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenReturn("officer1");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300);

        statusDTO.setStatus(ClaimStatus.IN_REVIEW);
        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        assertEquals(ClaimStatus.IN_REVIEW, claim.getStatus());
        assertEquals("officer1", claim.getProcessedBy());
        assertEquals(300, claim.getProcessedById());
        verify(claimRepository, times(1)).save(claim);
    }

    @Test
    void updateClaimStatus_toApproved() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(20000.0);
        statusDTO.setApprovalComments("Approved after verification");

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(policyClient.deductCoverage(eq(100), eq(20000.0))).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenReturn("officer1");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300);

        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        assertEquals(20000.0, claim.getApprovedAmount());
        assertEquals("Approved after verification", claim.getApprovalComments());
        verify(policyClient, times(1)).deductCoverage(100, 20000.0);
    }

    @Test
    void updateClaimStatus_toRejected() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.REJECTED);
        statusDTO.setRejectionReason("Invalid documents");

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenReturn("officer1");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300);

        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertEquals("Invalid documents", claim.getRejectionReason());
    }

    @Test
    void updateClaimStatus_inReview_fromNonSubmitted() {
        claim.setStatus(ClaimStatus.APPROVED);
        statusDTO.setStatus(ClaimStatus.IN_REVIEW);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("Only SUBMITTED claims can be picked up"));
    }

    @Test
    void updateClaimStatus_approved_noAmount() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(null);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("Approved amount is required"));
    }

    @Test
    void updateClaimStatus_approved_zeroAmount() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(0.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("must be greater than 0"));
    }

    @Test
    void updateClaimStatus_approved_exceedsClaimAmount() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(30000.0); // Claim amount is 25000

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("cannot exceed the claimed amount"));
    }

    @Test
    void updateClaimStatus_approved_exceedsRemainingSumInsured() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(20000.0);
        policyDTO.setRemainingSumInsured(10000.0); // Less than approved amount

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("exceeds remaining sum insured"));
    }

    @Test
    void updateClaimStatus_invalidStatus() {
        statusDTO.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("Invalid status update"));
    }

    @Test
    void updateClaimStatus_claimNotFound() {
        when(claimRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(99, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("Claim not found"));
    }

    @Test
    void updateClaimStatus_deductCoverageFails() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(20000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(policyClient.deductCoverage(eq(100), eq(20000.0)))
            .thenThrow(new RuntimeException("Policy service unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> claimService.updateClaimStatus(1, statusDTO, validToken));

        assertTrue(ex.getMessage().contains("Failed to deduct coverage"));
    }

    @Test
    void updateClaimStatus_withNullToken() {
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        statusDTO.setStatus(ClaimStatus.IN_REVIEW);
        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, null);

        assertNotNull(response);
        assertNull(claim.getProcessedBy());
        assertNull(claim.getProcessedById());
    }

    @Test
    void updateClaimStatus_tokenExtractionFails() {
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        statusDTO.setStatus(ClaimStatus.IN_REVIEW);
        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        // Processing continues even if token extraction fails
        verify(claimRepository, times(1)).save(claim);
    }

    // =============== GET IN REVIEW CLAIMS BY OFFICER TESTS ===============

    @Test
    void getInReviewClaimsByOfficer_success() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        claim.setProcessedById(300);

        when(claimRepository.findByProcessedByIdAndStatus(300, ClaimStatus.IN_REVIEW))
            .thenReturn(Arrays.asList(claim));

        List<ClaimResponse> responses = claimService.getInReviewClaimsByOfficer(300);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ClaimStatus.IN_REVIEW, responses.get(0).getStatus());
    }

    @Test
    void getInReviewClaimsByOfficer_empty() {
        when(claimRepository.findByProcessedByIdAndStatus(300, ClaimStatus.IN_REVIEW))
            .thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getInReviewClaimsByOfficer(300);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== GET PROCESSED CLAIMS BY OFFICER TESTS ===============

    @Test
    void getProcessedClaimsByOfficer_success() {
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setProcessedById(300);
        claim.setApprovedAmount(20000.0);

        Claim rejectedClaim = Claim.builder()
                .id(2)
                .policyId(101)
                .status(ClaimStatus.REJECTED)
                .processedById(300)
                .rejectionReason("Invalid documents")
                .build();

        when(claimRepository.findByProcessedByIdAndStatusIn(eq(300), anyList()))
            .thenReturn(Arrays.asList(claim, rejectedClaim));

        List<ClaimResponse> responses = claimService.getProcessedClaimsByOfficer(300);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void getProcessedClaimsByOfficer_empty() {
        when(claimRepository.findByProcessedByIdAndStatusIn(eq(300), anyList()))
            .thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getProcessedClaimsByOfficer(300);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // =============== GET CLAIMS BY AGENT TESTS ===============

    @Test
    void getClaimsByAgent_success() {
        when(policyClient.getPoliciesByAgent(400)).thenReturn(Arrays.asList(policyDTO));
        when(claimRepository.findByPolicyIdIn(anyList())).thenReturn(Arrays.asList(claim));

        List<ClaimResponse> responses = claimService.getClaimsByAgent(400);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getClaimsByAgent_noPolicies() {
        when(policyClient.getPoliciesByAgent(999)).thenReturn(Collections.emptyList());

        List<ClaimResponse> responses = claimService.getClaimsByAgent(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(claimRepository, never()).findByPolicyIdIn(anyList());
    }

    @Test
    void getClaimsByAgent_nullPolicies() {
        when(policyClient.getPoliciesByAgent(999)).thenReturn(null);

        List<ClaimResponse> responses = claimService.getClaimsByAgent(999);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(claimRepository, never()).findByPolicyIdIn(anyList());
    }

    // =============== NOTIFICATION CONTENT TESTS ===============

    @Test
    void updateClaimStatus_approvedWithPartialAmount_notificationContent() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        claim.setClaimAmount(25000.0);
        statusDTO.setStatus(ClaimStatus.APPROVED);
        statusDTO.setApprovedAmount(20000.0); // Partial approval

        Claim savedClaim = Claim.builder()
                .id(1)
                .policyId(100)
                .claimAmount(25000.0)
                .approvedAmount(20000.0)
                .status(ClaimStatus.APPROVED)
                .processedBy("officer1")
                .build();

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(policyClient.deductCoverage(eq(100), eq(20000.0))).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenReturn("officer1");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300);

        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        verify(kafkaTemplate, times(1)).send(eq("notification_topic"), any());
    }

    @Test
    void updateClaimStatus_rejectedWithReason_notificationContent() {
        claim.setStatus(ClaimStatus.IN_REVIEW);
        statusDTO.setStatus(ClaimStatus.REJECTED);
        statusDTO.setRejectionReason("Fraudulent claim");

        Claim savedClaim = Claim.builder()
                .id(1)
                .policyId(100)
                .status(ClaimStatus.REJECTED)
                .rejectionReason("Fraudulent claim")
                .processedBy("officer1")
                .build();

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);
        when(policyClient.getPolicyById(100)).thenReturn(policyDTO);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        when(jwtUtil.extractUsername(anyString())).thenReturn("officer1");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300);

        ClaimResponse response = claimService.updateClaimStatus(1, statusDTO, validToken);

        assertNotNull(response);
        assertEquals(ClaimStatus.REJECTED, response.getStatus());
    }
}
