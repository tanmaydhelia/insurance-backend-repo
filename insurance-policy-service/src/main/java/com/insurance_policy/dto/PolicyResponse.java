package com.insurance_policy.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.insurance_policy.model.PolicyStatus;

import lombok.Data;

@Data
public class PolicyResponse {
	private Integer id;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double premium;
    private Double remainingSumInsured;
    private Double coverageAmount;
    private PolicyStatus status;
    private Integer userId;
    private Integer agentId;
    private PlanResponse plan;
    private MemberDocumentResponse memberDocuments;
    
    // Renewal information
    private Long daysRemaining;
    private Boolean renewable;
    private LocalDateTime renewalRequestedAt;
    private String lastRenewalStatus;
}
