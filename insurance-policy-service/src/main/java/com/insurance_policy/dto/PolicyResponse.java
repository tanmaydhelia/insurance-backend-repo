package com.insurance_policy.dto;

import java.time.LocalDate;

import com.insurance_policy.model.PolicyStatus;

import lombok.Data;

@Data
public class PolicyResponse {
	private Integer id;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double premium;
    private PolicyStatus status;
    private Integer userId;
    private Integer agentId;
    private PlanResponse plan;
}
