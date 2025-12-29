package com.insurance_claims.service;

import java.util.List;

import com.insurance_claims.dto.ClaimRequest;
import com.insurance_claims.dto.ClaimResponse;
import com.insurance_claims.dto.ClaimStatusDTO;

public interface ClaimService {
	
	public ClaimResponse submitClaim(ClaimRequest request);
	
	public ClaimResponse getClaimById(Integer id);
	
	public List<ClaimResponse> getClaimsByMember(Integer userId);
	
	public List<ClaimResponse> getClaimsByProvider(Integer hospitalId);
	
	public List<ClaimResponse> getOpenClaims();
	
	public ClaimResponse updateClaimStatus(Integer id, ClaimStatusDTO status);
}
