package com.insurance_claims.dto;

import com.insurance_claims.model.ClaimStatus;

import lombok.Data;

@Data
public class ClaimStatusDTO {
	private ClaimStatus status;
    private String rejectionReason;
    
    // Amount approved by claims officer (required when status is APPROVED)
    private Double approvedAmount;
    
    // Optional comments provided by claims officer when approving
    private String approvalComments;
}
