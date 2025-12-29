package com.insurance_policy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance_policy.model.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
	List<Policy> findByUserId(Integer userId);
    List<Policy> findByAgentId(Integer agentId);
}
