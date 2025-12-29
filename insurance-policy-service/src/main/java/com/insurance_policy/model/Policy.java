package com.insurance_policy.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    private LocalDate startDate;
    private LocalDate endDate;
    private Double premium;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    private Integer userId;
    private Integer agentId;
    
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan insurancePlan;
    
    @PrePersist
    public void generatePolicyNumber() {
        if (this.policyNumber == null) {
            this.policyNumber = UUID.randomUUID().toString();
        }
    }
}
