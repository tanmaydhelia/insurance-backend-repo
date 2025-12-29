package com.insurance_claims.dto;

import com.insurance_claims.model.SubmissionSource;

import lombok.Data;

@Data
public class ClaimRequest {
	private Integer policyId;
    private String diagnosis;
    private Double claimAmount;
    private Integer hospitalId;
    private SubmissionSource submissionSource;
}
