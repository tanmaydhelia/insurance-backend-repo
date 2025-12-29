package com.insurance_claims.dto;

import com.insurance_claims.model.ClaimStatus;

import lombok.Data;

@Data
public class ClaimStatusDTO {
	private ClaimStatus status;
    private String rejectionReason;
}
