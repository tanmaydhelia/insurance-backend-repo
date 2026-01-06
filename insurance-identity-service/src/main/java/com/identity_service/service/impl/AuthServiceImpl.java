package com.identity_service.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.identity_service.dto.ChangePasswordRequest;
import com.identity_service.dto.CreateUserRequest;
import com.identity_service.dto.NotificationEvent;
import com.identity_service.dto.UserResponse;
import com.identity_service.model.ERole;
import com.identity_service.model.UserCredential;
import com.identity_service.repository.UserCredentialRepository;
import com.identity_service.security.JwtService;
import com.identity_service.service.AuthService;
import com.identity_service.util.EmailTemplateHelper;

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
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
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
    	
    	NotificationEvent event = new NotificationEvent(
                creds.getId(),
                "Welcome to RestO'Sure - Your Health Insurance Partner",
                EmailTemplateHelper.formatWelcomeEmail(creds.getName())
            );
       	kafkaTemplate.send("notification_topic", event);
    	
    	return "User Added to the system with ID: " + creds.getId();
    }
    
    public String generateToken(String email) {
    	
    	UserCredential user = userCredRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    	
    	return jwtService.generateToken(user.getName(), user.getRole().toString(), email, user.getId());
    }
    
    public void validateToken(String token) {
    	jwtService.validateToken(token);
    }
    
    public String changePassword(ChangePasswordRequest request) {
        UserCredential user = userCredRepo.findByEmail(request.getEmail())
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

	public UserCredential getUserById(Integer id) {
		return userCredRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
	}
	
	// ==================== ADMIN METHODS ====================
	
	/**
	 * Create a new user (Admin only) - for agents, officers, providers
	 */
	public UserResponse createUserByAdmin(CreateUserRequest request) {
	    // Validate role - admins cannot create regular users through this endpoint
	    if (request.getRole() == ERole.ROLE_USER) {
	        throw new RuntimeException("Use /auth/register for creating regular users");
	    }
	    
	    // Check if email already exists
	    if (userCredRepo.existsByEmail(request.getEmail())) {
	        throw new RuntimeException("Email already registered: " + request.getEmail());
	    }
	    
	    UserCredential user = new UserCredential();
	    user.setName(request.getName());
	    user.setEmail(request.getEmail());
	    user.setPassword(passwordEncoder.encode(request.getPassword()));
	    user.setRole(request.getRole());
	    user.setActive(true);
	    
    	
    	UserCredential savedUser = userCredRepo.save(user);
    	
    	// Send notification
    	NotificationEvent event = new NotificationEvent(
    	    savedUser.getId(),
    	    "Account Created - RestO'Sure",
    	    EmailTemplateHelper.formatAccountCreatedEmail(savedUser.getName(), getRoleName(savedUser.getRole()))
    	);
    	kafkaTemplate.send("notification_topic", event);	    return mapToUserResponse(savedUser);
	}
	
	/**
	 * Suspend a user account (Admin only)
	 */
	public UserResponse suspendUser(Integer userId) {
	    UserCredential user = userCredRepo.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	    
	    if (user.getRole() == ERole.ROLE_ADMIN) {
	        throw new RuntimeException("Cannot suspend an admin account");
	    }
	    
    	
    	user.setActive(false);
    	UserCredential savedUser = userCredRepo.save(user);
    	
    	// Send notification
    	NotificationEvent event = new NotificationEvent(
    	    savedUser.getId(),
    	    "Account Suspended - RestO'Sure",
    	    EmailTemplateHelper.formatAccountSuspendedEmail(savedUser.getName())
    	);
    	kafkaTemplate.send("notification_topic", event);	    return mapToUserResponse(savedUser);
	}
	
	/**
	 * Activate a suspended user account (Admin only)
	 */
	public UserResponse activateUser(Integer userId) {
	    UserCredential user = userCredRepo.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	    
    	
    	user.setActive(true);
    	UserCredential savedUser = userCredRepo.save(user);
    	
    	// Send notification
    	NotificationEvent event = new NotificationEvent(
    	    savedUser.getId(),
    	    "Account Reactivated - RestO'Sure",
    	    EmailTemplateHelper.formatAccountReactivatedEmail(savedUser.getName())
    	);
    	kafkaTemplate.send("notification_topic", event);	    return mapToUserResponse(savedUser);
	}
	
	/**
	 * Get all users by role (Admin only)
	 */
	public List<UserResponse> getUsersByRole(ERole role) {
	    return userCredRepo.findByRole(role).stream()
	            .map(this::mapToUserResponse)
	            .collect(Collectors.toList());
	}
	
	/**
	 * Get all staff users (agents, officers, providers) - excludes regular users
	 */
	public List<UserResponse> getAllStaffUsers() {
	    List<ERole> staffRoles = Arrays.asList(
	        ERole.ROLE_AGENT, 
	        ERole.ROLE_CLAIMS_OFFICER, 
	        ERole.ROLE_PROVIDER,
	        ERole.ROLE_ADMIN
	    );
	    return userCredRepo.findByRoleIn(staffRoles).stream()
	            .map(this::mapToUserResponse)
	            .collect(Collectors.toList());
	}
	
	/**
	 * Get all users (Admin only)
	 */
	public List<UserResponse> getAllUsers() {
	    return userCredRepo.findAll().stream()
	            .map(this::mapToUserResponse)
	            .collect(Collectors.toList());
	}
	
	/**
	 * Update user details (Admin only)
	 */
	public UserResponse updateUser(Integer userId, CreateUserRequest request) {
	    UserCredential user = userCredRepo.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	    
	    // Update fields if provided
	    if (request.getName() != null && !request.getName().isEmpty()) {
	        user.setName(request.getName());
	    }
	    if (request.getEmail() != null && !request.getEmail().isEmpty()) {
	        // Check if new email is already taken by another user
	        if (!user.getEmail().equals(request.getEmail()) && userCredRepo.existsByEmail(request.getEmail())) {
	            throw new RuntimeException("Email already in use: " + request.getEmail());
	        }
	        user.setEmail(request.getEmail());
	    }
	    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
	        user.setPassword(passwordEncoder.encode(request.getPassword()));
	    }
	    if (request.getRole() != null) {
	        user.setRole(request.getRole());
	    }
	    
	    UserCredential savedUser = userCredRepo.save(user);
	    return mapToUserResponse(savedUser);
	}
	
	/**
	 * Delete user (Admin only) - use with caution
	 */
	public void deleteUser(Integer userId) {
	    UserCredential user = userCredRepo.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	    
	    if (user.getRole() == ERole.ROLE_ADMIN) {
	        throw new RuntimeException("Cannot delete an admin account");
	    }
	    
	    userCredRepo.delete(user);
	}
	
	// Helper methods
	private UserResponse mapToUserResponse(UserCredential user) {
	    return UserResponse.builder()
	            .id(user.getId())
	            .name(user.getName())
	            .email(user.getEmail())
	            .role(user.getRole())
	            .active(user.getActive())
	            .build();
	}
	
	private String getRoleName(ERole role) {
	    switch (role) {
	        case ROLE_AGENT: return "Insurance Agent";
	        case ROLE_CLAIMS_OFFICER: return "Claims Officer";
	        case ROLE_PROVIDER: return "Healthcare Provider";
	        case ROLE_ADMIN: return "Administrator";
	        default: return "User";
	    }
	}
}
