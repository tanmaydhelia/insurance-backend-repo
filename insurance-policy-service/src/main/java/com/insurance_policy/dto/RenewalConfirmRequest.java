package com.insurance_policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RenewalConfirmRequest {
    private String orderId;
    private String paymentId;
    private Boolean success;
}
