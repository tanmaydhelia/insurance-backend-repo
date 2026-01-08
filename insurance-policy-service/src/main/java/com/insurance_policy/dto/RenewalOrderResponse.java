package com.insurance_policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RenewalOrderResponse {
    private String orderId;
    private Double amount;
    private String currency;
    private Integer policyId;
    private String status;
}
