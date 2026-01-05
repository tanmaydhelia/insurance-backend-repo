package com.insurance_policy.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.insurance_policy.dto.MemberDocumentResponse;
import com.insurance_policy.model.MemberDocument;
import com.insurance_policy.repository.MemberDocumentRepository;
import com.insurance_policy.service.FileUploadService;
import com.insurance_policy.service.MemberDocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDocumentServiceImpl implements MemberDocumentService {
    
    private final MemberDocumentRepository memberDocumentRepository;
    private final FileUploadService fileUploadService;
    
    @Override
    public MemberDocumentResponse submitMemberDocuments(
            Integer userId,
            String aadhaarNumber,
            MultipartFile photo,
            MultipartFile medicalCheckupDoc) {
        
        // Validate Aadhaar number
        validateAadhaarNumber(aadhaarNumber);
        
        // Check if documents already exist for this user
        if (memberDocumentRepository.existsByUserId(userId)) {
            throw new RuntimeException("Member documents already exist for user: " + userId + ". Use update endpoint instead.");
        }
        
        // Check if Aadhaar is already registered
        if (memberDocumentRepository.existsByAadhaarNumber(aadhaarNumber)) {
            throw new RuntimeException("Aadhaar number is already registered with another member.");
        }
        
        // Upload files to Cloudinary
        String photoUrl = null;
        String medicalDocUrl = null;
        
        if (photo != null && !photo.isEmpty()) {
            photoUrl = fileUploadService.uploadFile(photo, "member_photos");
        }
        
        if (medicalCheckupDoc != null && !medicalCheckupDoc.isEmpty()) {
            medicalDocUrl = fileUploadService.uploadFile(medicalCheckupDoc, "medical_documents");
        }
        
        // Create and save member document
        MemberDocument memberDocument = MemberDocument.builder()
                .userId(userId)
                .aadhaarNumber(aadhaarNumber)
                .photoUrl(photoUrl)
                .medicalCheckupDocUrl(medicalDocUrl)
                .build();
        
        MemberDocument savedDocument = memberDocumentRepository.save(memberDocument);
        
        return mapToResponse(savedDocument);
    }
    
    @Override
    public MemberDocumentResponse getMemberDocuments(Integer userId) {
        MemberDocument document = memberDocumentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member documents not found for user: " + userId));
        return mapToResponse(document);
    }
    
    @Override
    public MemberDocumentResponse updateMemberDocuments(
            Integer userId,
            String aadhaarNumber,
            MultipartFile photo,
            MultipartFile medicalCheckupDoc) {
        
        MemberDocument existingDocument = memberDocumentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member documents not found for user: " + userId));
        
        // Update Aadhaar if provided and different
        if (aadhaarNumber != null && !aadhaarNumber.isEmpty()) {
            validateAadhaarNumber(aadhaarNumber);
            
            // Check if new Aadhaar is already used by another member
            if (!existingDocument.getAadhaarNumber().equals(aadhaarNumber) 
                    && memberDocumentRepository.existsByAadhaarNumber(aadhaarNumber)) {
                throw new RuntimeException("Aadhaar number is already registered with another member.");
            }
            existingDocument.setAadhaarNumber(aadhaarNumber);
        }
        
        // Update photo if provided
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileUploadService.uploadFile(photo, "member_photos");
            existingDocument.setPhotoUrl(photoUrl);
        }
        
        // Update medical document if provided
        if (medicalCheckupDoc != null && !medicalCheckupDoc.isEmpty()) {
            String medicalDocUrl = fileUploadService.uploadFile(medicalCheckupDoc, "medical_documents");
            existingDocument.setMedicalCheckupDocUrl(medicalDocUrl);
        }
        
        MemberDocument updatedDocument = memberDocumentRepository.save(existingDocument);
        
        return mapToResponse(updatedDocument);
    }
    
    @Override
    public boolean hasMemberDocuments(Integer userId) {
        return memberDocumentRepository.existsByUserId(userId);
    }
    
    @Override
    public void validateAadhaarNumber(String aadhaarNumber) {
        if (aadhaarNumber == null || aadhaarNumber.isEmpty()) {
            throw new RuntimeException("Aadhaar number is required.");
        }
        
        // Aadhaar should be exactly 12 digits
        if (!aadhaarNumber.matches("^[0-9]{12}$")) {
            throw new RuntimeException("Invalid Aadhaar number. It should be exactly 12 digits.");
        }
        
        // First digit should not be 0 or 1
        if (aadhaarNumber.startsWith("0") || aadhaarNumber.startsWith("1")) {
            throw new RuntimeException("Invalid Aadhaar number. It should not start with 0 or 1.");
        }
    }
    
    private MemberDocumentResponse mapToResponse(MemberDocument document) {
        return MemberDocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .aadhaarNumber(maskAadhaarNumber(document.getAadhaarNumber()))
                .photoUrl(document.getPhotoUrl())
                .medicalCheckupDocUrl(document.getMedicalCheckupDocUrl())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    
    // Mask Aadhaar for security: XXXX-XXXX-1234
    private String maskAadhaarNumber(String aadhaarNumber) {
        if (aadhaarNumber == null || aadhaarNumber.length() != 12) {
            return aadhaarNumber;
        }
        return "XXXX-XXXX-" + aadhaarNumber.substring(8);
    }
}
