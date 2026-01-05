package com.insurance_claims.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.insurance_claims.client.PolicyClient;
import com.insurance_claims.dto.ClaimRequest;
import com.insurance_claims.dto.ClaimResponse;
import com.insurance_claims.dto.ClaimStatusDTO;
import com.insurance_claims.dto.NotificationEvent;
import com.insurance_claims.dto.PolicyDTO;
import com.insurance_claims.model.Claim;
import com.insurance_claims.model.ClaimStatus;
import com.insurance_claims.model.SubmissionSource;
import com.insurance_claims.repository.ClaimRepository;
import com.insurance_claims.service.ClaimService;
import com.insurance_claims.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService{
	
	@Autowired
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;
    private final JwtUtil jwtUtil;

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
                .documentUrl(request.getDocumentUrl())
                .date(LocalDate.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);
        
        Integer userId = policyClient.getPolicyById(request.getPolicyId()).getUserId();

        NotificationEvent event = new NotificationEvent(
            userId,
            "Claim Submitted Successfully",
            "Your claim for " + request.getDiagnosis() + " has been received. Claim ID: " + savedClaim.getId()
        );
        kafkaTemplate.send("notification_topic", event);
        
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
        // Return ONLY claims with status = SUBMITTED
        // These are unassigned claims waiting for an officer to pick them up
        return claimRepository.findByStatusIn(Arrays.asList(ClaimStatus.SUBMITTED))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse updateClaimStatus(Integer id, ClaimStatusDTO statusDTO, String token) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Validate allowed status transitions
        if (statusDTO.getStatus() == ClaimStatus.IN_REVIEW || 
            statusDTO.getStatus() == ClaimStatus.APPROVED || 
            statusDTO.getStatus() == ClaimStatus.REJECTED) {
            
            // Validate that only SUBMITTED claims can be picked up for review
            if (statusDTO.getStatus() == ClaimStatus.IN_REVIEW) {
                if (claim.getStatus() != ClaimStatus.SUBMITTED) {
                    throw new RuntimeException("Only SUBMITTED claims can be picked up for review. Current status: " + claim.getStatus());
                }
            }
            
            // Update status
            claim.setStatus(statusDTO.getStatus());
            
            // Set rejection reason if status is REJECTED
            if (statusDTO.getStatus() == ClaimStatus.REJECTED) {
                claim.setRejectionReason(statusDTO.getRejectionReason());
            } else {
                claim.setRejectionReason(null);
            }
            
            // Track Claims Officer for IN_REVIEW, APPROVED, or REJECTED status
            if (statusDTO.getStatus() == ClaimStatus.IN_REVIEW ||
                statusDTO.getStatus() == ClaimStatus.APPROVED || 
                statusDTO.getStatus() == ClaimStatus.REJECTED) {
                
                if (token != null && !token.isEmpty()) {
                    try {
                        // Extract officer information from JWT token
                        String officerName = jwtUtil.extractUsername(token);
                        Integer officerId = jwtUtil.extractUserId(token);
                        
                        // Set officer info for all status changes
                        claim.setProcessedBy(officerName);
                        claim.setProcessedById(officerId);
                        claim.setProcessedDate(LocalDateTime.now());
                    } catch (Exception e) {
                        // If token extraction fails, log but continue processing
                        System.err.println("Failed to extract user info from token: " + e.getMessage());
                    }
                }
            }
        } else {
            throw new RuntimeException("Invalid status update. Allowed: IN_REVIEW, APPROVED, REJECTED");
        }
        
        Claim savedClaim = claimRepository.save(claim);
        
        // Send notification to user
        String subject = "Claim Status Update: " + savedClaim.getStatus();
        String body = "Your claim #" + id + " has been " + savedClaim.getStatus();
        
        if(savedClaim.getStatus() == ClaimStatus.REJECTED) {
            body += ". Reason: " + savedClaim.getRejectionReason();
        }
        
        if (savedClaim.getProcessedBy() != null) {
            body += " by " + savedClaim.getProcessedBy();
        }
        
        Integer userId = policyClient.getPolicyById(claim.getPolicyId()).getUserId();

        NotificationEvent event = new NotificationEvent(
        	userId,
            subject,
            body
        );
        kafkaTemplate.send("notification_topic", event);
        
        return mapToResponse(savedClaim);
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
                .documentUrl(claim.getDocumentUrl())
                .date(claim.getDate())
                .processedBy(claim.getProcessedBy())
                .processedById(claim.getProcessedById())
                .processedDate(claim.getProcessedDate())
                .build();
    }

    @Override
    public List<ClaimResponse> getInReviewClaimsByOfficer(Integer officerId) {
        return claimRepository.findByProcessedByIdAndStatus(officerId, ClaimStatus.IN_REVIEW)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClaimResponse> getProcessedClaimsByOfficer(Integer officerId) {
        return claimRepository.findByProcessedByIdAndStatusIn(
                officerId, 
                Arrays.asList(ClaimStatus.APPROVED, ClaimStatus.REJECTED))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
