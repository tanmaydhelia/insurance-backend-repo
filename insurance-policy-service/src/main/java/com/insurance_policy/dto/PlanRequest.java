package com.insurance_policy.dto;

import lombok.Data;

@Data
public class PlanRequest {
	private String name;
    private String description;
    private Double basePremium;
    private Double coverageAmount;
}
