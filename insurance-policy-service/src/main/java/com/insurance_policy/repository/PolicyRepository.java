package com.insurance_policy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.insurance_policy.model.Policy;
import com.insurance_policy.model.PolicyStatus;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
	List<Policy> findByUserId(Integer userId);
    List<Policy> findByAgentId(Integer agentId);
    
    // Find policy by policy number
    Policy findByPolicyNumber(String policyNumber);
    
    // Find policies expiring within N days
    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate BETWEEN :today AND :futureDate")
    List<Policy> findExpiringPolicies(
        @Param("status") PolicyStatus status,
        @Param("today") LocalDate today,
        @Param("futureDate") LocalDate futureDate
    );
    
    // Find expired policies (past end date but still ACTIVE status)
    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate < :today")
    List<Policy> findExpiredPolicies(
        @Param("status") PolicyStatus status,
        @Param("today") LocalDate today
    );
    
    // For renewal: Find policies expiring between two dates
    List<Policy> findByEndDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, PolicyStatus status);
    
    // For scheduler: Find policies that have already expired but are still marked ACTIVE
    List<Policy> findByEndDateBeforeAndStatus(LocalDate date, PolicyStatus status);
    
    // For scheduler reminders: Find policies expiring on a specific date
    List<Policy> findByEndDateAndStatus(LocalDate endDate, PolicyStatus status);
}
