package com.insurance_policy.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDocumentResponse {
    private Integer id;
    private Integer userId;
    private String aadhaarNumber;
    private String photoUrl;
    private String medicalCheckupDocUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
