package com.insurance_policy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.insurance_policy.dto.MemberDocumentResponse;
import com.insurance_policy.service.MemberDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/policy/member-documents")
@RequiredArgsConstructor
public class MemberDocumentController {
    
    private final MemberDocumentService memberDocumentService;
    
    /**
     * Submit member documents (Aadhaar, photo, medical checkup doc)
     * This should be called when a user is buying a policy for the first time
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDocumentResponse> submitMemberDocuments(
            @RequestParam("userId") Integer userId,
            @RequestParam("aadhaarNumber") String aadhaarNumber,
            @RequestParam(value = "photo", required = true) MultipartFile photo,
            @RequestParam(value = "medicalCheckupDoc", required = true) MultipartFile medicalCheckupDoc) {
        
        MemberDocumentResponse response = memberDocumentService.submitMemberDocuments(
            userId, aadhaarNumber, photo, medicalCheckupDoc
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get member documents by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<MemberDocumentResponse> getMemberDocuments(@PathVariable Integer userId) {
        return ResponseEntity.ok(memberDocumentService.getMemberDocuments(userId));
    }
    
    /**
     * Update member documents
     */
    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDocumentResponse> updateMemberDocuments(
            @PathVariable Integer userId,
            @RequestParam(value = "aadhaarNumber", required = false) String aadhaarNumber,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "medicalCheckupDoc", required = false) MultipartFile medicalCheckupDoc) {
        
        MemberDocumentResponse response = memberDocumentService.updateMemberDocuments(
            userId, aadhaarNumber, photo, medicalCheckupDoc
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if member has submitted documents
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> hasMemberDocuments(@PathVariable Integer userId) {
        return ResponseEntity.ok(memberDocumentService.hasMemberDocuments(userId));
    }
}
