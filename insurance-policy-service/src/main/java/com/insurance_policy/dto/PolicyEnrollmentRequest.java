package com.insurance_policy.dto;

import lombok.Data;

@Data
public class PolicyEnrollmentRequest {
	private Integer userId;
    private Integer planId;
    private Integer agentId;
}

