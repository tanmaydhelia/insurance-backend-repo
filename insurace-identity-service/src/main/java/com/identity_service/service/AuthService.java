package com.identity_service.service;

import org.springframework.stereotype.Service;

import com.identity_service.dto.ChangePasswordRequest;
import com.identity_service.model.UserCredential;



@Service
public interface AuthService {
	public String loginWithGoogle(String idTokenString);
    
    public String saveUser(UserCredential creds);
    
    public String generateToken(String username);
    
    public void validateToken(String token);
    
    public String changePassword(ChangePasswordRequest request);
}
