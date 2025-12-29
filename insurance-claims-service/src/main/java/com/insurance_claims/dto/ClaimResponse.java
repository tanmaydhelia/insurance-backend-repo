package com.insurance_claims.dto;

import java.time.LocalDate;

import com.insurance_claims.model.ClaimStatus;
import com.insurance_claims.model.SubmissionSource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimResponse {
    private Integer id;
    private Integer policyId;
    private Integer hospitalId;
    private String diagnosis;
    private Double claimAmount;
    private ClaimStatus status;
    private SubmissionSource submissionSource;
    private String rejectionReason;
    private LocalDate date;
}