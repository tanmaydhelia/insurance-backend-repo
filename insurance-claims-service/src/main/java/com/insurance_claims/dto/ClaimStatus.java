package com.insurance_claims.dto;

import lombok.Data;

@Data
public class ClaimStatus {
	private ClaimStatus status;
    private String rejectionReason;
}
