package com.identity_service.service.impl;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.identity_service.dto.ChangePasswordRequest;
import com.identity_service.model.ERole;
import com.identity_service.model.UserCredential;
import com.identity_service.repository.UserCredentialRepository;
import com.identity_service.security.JwtService;
import com.identity_service.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService{
	@Autowired
    private UserCredentialRepository userCredRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    
    private String googleClientId = "575184160466-1roir0tpclglg6jgee329l32q7svu500.apps.googleusercontent.com";
    
    public String loginWithGoogle(String idTokenString) {
    	log.info("Starting Google Token Validation... ");
        log.info("Using Client ID: {}", googleClientId);
        log.info("Received Token (prefix): {}...", idTokenString.substring(0, Math.min(idTokenString.length(), 20)));
    	
    	try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
												.setAudience(Collections.singletonList(googleClientId))
												.build();
			
			GoogleIdToken idToken = verifier.verify(idTokenString);
			
			if(idToken != null) {
				GoogleIdToken.Payload payload = idToken.getPayload();
				log.info("Token Verified Successfully for email: {}", payload.getEmail());

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                
                UserCredential user = userCredRepo.findByEmail(email).orElseGet(() -> {
                	log.info("New Google user detected. Registering: {}", email);
                    UserCredential newUser = new UserCredential();
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setRole(ERole.ROLE_USER);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return userCredRepo.save(newUser);
                });
                
                return jwtService.generateToken(user.getName(), user.getRole().toString(), user.getEmail(), user.getId());
			} else {
				log.error("GoogleIdTokenVerifier.verify() returned NULL. Check if Client ID matches 'aud' in token.");
                throw new RuntimeException("Invalid Google Token");
            }
			
		} catch (Exception e) {
			log.error("Exception during Google verification: {}", e.getMessage(), e);
			throw new RuntimeException("Google Authentication Failed: " + e.getMessage());
		}
    }
    
    public String saveUser(UserCredential creds) {
    	creds.setPassword(passwordEncoder.encode(creds.getPassword()));
    
    	if (creds.getRole() == null || creds.getRole().toString().isEmpty()) {
            creds.setRole(ERole.ROLE_USER);
        }
    	
    	userCredRepo.save(creds);
    	return "User Added to the system";
    }
    
    public String generateToken(String username) {
    	
    	UserCredential user = userCredRepo.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    	
    	return jwtService.generateToken(username, user.getRole().toString(), user.getEmail(), user.getId());
    }
    
    public void validateToken(String token) {
    	jwtService.validateToken(token);
    }
    
    public String changePassword(ChangePasswordRequest request) {
        UserCredential user = userCredRepo.findByName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userCredRepo.save(user);

        return "Password changed successfully";
    }
    
    public UserCredential getUserByEmail(String email) {
        return userCredRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
