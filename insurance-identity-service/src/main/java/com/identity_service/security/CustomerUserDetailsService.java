package com.identity_service.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.identity_service.model.CustomUserDetails;
import com.identity_service.model.UserCredential;
import com.identity_service.repository.UserCredentialRepository;

public class CustomerUserDetailsService implements UserDetailsService{
	@Autowired
	private UserCredentialRepository userCredRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserCredential> credential = userCredRepo.findByEmail(username);
		
		UserCredential user = credential.orElseThrow(() -> new UsernameNotFoundException("user not found with email: " + username));
		
		// Check if user account is active (null is treated as active for backward compatibility)
		if (user.getActive() != null && !user.getActive()) {
			throw new UsernameNotFoundException("Account is suspended. Please contact support.");
		}
		
		return new CustomUserDetails(user);
	}
}
