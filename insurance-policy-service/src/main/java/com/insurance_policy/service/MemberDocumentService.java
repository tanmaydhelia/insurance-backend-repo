package com.insurance_policy.service;

import org.springframework.web.multipart.MultipartFile;

import com.insurance_policy.dto.MemberDocumentResponse;

public interface MemberDocumentService {
    
    MemberDocumentResponse submitMemberDocuments(
        Integer userId,
        String aadhaarNumber,
        MultipartFile photo,
        MultipartFile medicalCheckupDoc
    );
    
    MemberDocumentResponse getMemberDocuments(Integer userId);
    
    MemberDocumentResponse updateMemberDocuments(
        Integer userId,
        String aadhaarNumber,
        MultipartFile photo,
        MultipartFile medicalCheckupDoc
    );
    
    boolean hasMemberDocuments(Integer userId);
    
    void validateAadhaarNumber(String aadhaarNumber);
}
