package com.insurance_policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance_policy.model.InsurancePlan;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Integer>{
	
}
