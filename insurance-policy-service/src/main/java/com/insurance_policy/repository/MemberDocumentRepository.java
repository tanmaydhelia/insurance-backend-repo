package com.insurance_policy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance_policy.model.MemberDocument;

public interface MemberDocumentRepository extends JpaRepository<MemberDocument, Integer> {
    
    Optional<MemberDocument> findByUserId(Integer userId);
    
    boolean existsByUserId(Integer userId);
    
    boolean existsByAadhaarNumber(String aadhaarNumber);
}
