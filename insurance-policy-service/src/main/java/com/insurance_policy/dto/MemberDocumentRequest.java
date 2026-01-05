package com.insurance_policy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDocumentRequest {
    private Integer userId;
    private String aadhaarNumber;
    // Photo and medical document will be handled as MultipartFile in the controller
}
