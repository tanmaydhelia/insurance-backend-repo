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
		
		Optional<UserCredential> credential = userCredRepo.findByName(username);
		
		return credential.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("user not found with name :" + username));
	}
}
