package com.insurance_claims.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "claims")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Claim {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer policyId;
    private Integer hospitalId;
    private String diagnosis;
    private Double claimAmount;
    
    // Amount approved by claims officer (may be less than or equal to claimAmount)
    @Column(name = "approved_amount")
    private Double approvedAmount;
    
    // Comments provided by claims officer when approving the claim
    @Column(name = "approval_comments", length = 1000)
    private String approvalComments;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    private String rejectionReason;
    private LocalDate date;
    private String documentUrl;
    
    // Fields to track Claims Officer processing
    @Column(name = "processed_by")
    private String processedBy;
    
    @Column(name = "processed_by_id")
    private Integer processedById;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
}
