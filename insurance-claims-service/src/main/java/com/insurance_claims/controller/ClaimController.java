package com.insurance_claims.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance_claims.dto.ClaimRequest;
import com.insurance_claims.dto.ClaimResponse;
import com.insurance_claims.dto.ClaimStatusDTO;
import com.insurance_claims.service.ClaimService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {
	private final ClaimService claimService;

    @PostMapping("/submit")
    public ResponseEntity<ClaimResponse> submitClaim(@RequestBody ClaimRequest request) {
        return new ResponseEntity<>(claimService.submitClaim(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaim(@PathVariable Integer id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @GetMapping("/member/{userId}")
    public ResponseEntity<List<ClaimResponse>> getMemberClaims(@PathVariable Integer userId) {
        return ResponseEntity.ok(claimService.getClaimsByMember(userId));
    }
    
    @GetMapping("/provider/{hospitalId}")
    public ResponseEntity<List<ClaimResponse>> getProviderClaims(@PathVariable Integer hospitalId) {
        return ResponseEntity.ok(claimService.getClaimsByProvider(hospitalId));
    }

    @GetMapping("/open")
    public ResponseEntity<List<ClaimResponse>> getOpenClaims() {
        return ResponseEntity.ok(claimService.getOpenClaims());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ClaimResponse> updateStatus(@PathVariable Integer id, @RequestBody ClaimStatusDTO statusDTO) {
        return ResponseEntity.ok(claimService.updateClaimStatus(id, statusDTO));
    }
}
