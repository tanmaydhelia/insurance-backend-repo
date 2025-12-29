package com.insurance_claims.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance_claims.model.Claim;
import com.insurance_claims.model.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim,Integer> {
	List<Claim> findByStatusIn(List<ClaimStatus> statuses);
    List<Claim> findByPolicyIdIn(List<Long> policyIds);
    List<Claim> findByHospitalId(Long hospitalId);
}
