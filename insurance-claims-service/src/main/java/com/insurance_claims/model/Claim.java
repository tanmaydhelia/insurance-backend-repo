package com.insurance_claims.model;

import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    private SubmissionSource submissionSource;

    private String rejectionReason;
    private LocalDate date;
}
