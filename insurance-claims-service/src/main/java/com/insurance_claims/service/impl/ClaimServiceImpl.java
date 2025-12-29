package com.insurance_claims.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.insurance_claims.client.PolicyClient;
import com.insurance_claims.dto.ClaimRequest;
import com.insurance_claims.dto.ClaimResponse;
import com.insurance_claims.dto.ClaimStatusDTO;
import com.insurance_claims.dto.PolicyDTO;
import com.insurance_claims.model.Claim;
import com.insurance_claims.model.ClaimStatus;
import com.insurance_claims.model.SubmissionSource;
import com.insurance_claims.repository.ClaimRepository;
import com.insurance_claims.service.ClaimService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService{
	
	private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;

    public ClaimResponse submitClaim(ClaimRequest request) {
        if (request.getSubmissionSource() == SubmissionSource.PROVIDER && request.getHospitalId() == null) {
            throw new RuntimeException("Hospital ID is required for PROVIDER submission.");
        }

        PolicyDTO policy = policyClient.getPolicyById(request.getPolicyId());
        
        if (policy == null) {
            throw new RuntimeException("Policy not found");
        }
        
        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
            throw new RuntimeException("Cannot submit claim. Policy is not ACTIVE.");
        }

        Claim claim = Claim.builder()
                .policyId(request.getPolicyId())
                .hospitalId(request.getHospitalId())
                .diagnosis(request.getDiagnosis())
                .claimAmount(request.getClaimAmount())
                .status(ClaimStatus.SUBMITTED)
                .submissionSource(request.getSubmissionSource())
                .date(LocalDate.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim);
    }

    public ClaimResponse getClaimById(Integer id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        return mapToResponse(claim);
    }

    public List<ClaimResponse> getClaimsByMember(Integer userId) {
        List<PolicyDTO> policies = policyClient.getPoliciesByMember(userId);
        List<Integer> policyIds = policies.stream()
                .map(PolicyDTO::getId)
                .collect(Collectors.toList());

        if (policyIds.isEmpty()) {
            return List.of();
        }

        return claimRepository.findByPolicyIdIn(policyIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClaimResponse> getClaimsByProvider(Integer hospitalId) {
        return claimRepository.findByHospitalId(hospitalId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClaimResponse> getOpenClaims() {
        return claimRepository.findByStatusIn(Arrays.asList(ClaimStatus.SUBMITTED, ClaimStatus.IN_REVIEW))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse updateClaimStatus(Integer id, ClaimStatusDTO statusDTO) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (statusDTO.getStatus() == ClaimStatus.IN_REVIEW || statusDTO.getStatus() == ClaimStatus.APPROVED || statusDTO.getStatus() == ClaimStatus.REJECTED) {
            claim.setStatus(statusDTO.getStatus());
            if (statusDTO.getStatus() == ClaimStatus.REJECTED) {
                claim.setRejectionReason(statusDTO.getRejectionReason());
            } else {
                claim.setRejectionReason(null);
            }
        } else {
            throw new RuntimeException("Invalid status update. Allowed: IN_REVIEW, APPROVED, REJECTED");
        }

        return mapToResponse(claimRepository.save(claim));
    }

    private ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .policyId(claim.getPolicyId())
                .hospitalId(claim.getHospitalId())
                .diagnosis(claim.getDiagnosis())
                .claimAmount(claim.getClaimAmount())
                .status(claim.getStatus())
                .submissionSource(claim.getSubmissionSource())
                .rejectionReason(claim.getRejectionReason())
                .date(claim.getDate())
                .build();
    }

	
}
