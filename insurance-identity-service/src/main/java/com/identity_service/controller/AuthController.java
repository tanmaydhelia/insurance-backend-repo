package com.identity_service.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.identity_service.dto.AuthRequest;
import com.identity_service.dto.ChangePasswordRequest;
import com.identity_service.model.UserCredential;
import com.identity_service.service.impl.AuthServiceImpl;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private AuthServiceImpl authService;
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping("/register")
	public String addNewUser(@RequestBody UserCredential user) {
		return authService.saveUser(user);
	}
	
	@PostMapping("/token")
	public String getToken(@RequestBody AuthRequest authRequest) {
		Authentication authenticate  = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
		
		if(authenticate.isAuthenticated()) {
			return authService.generateToken(authRequest.getUsername());
		}
		else {
			throw new RuntimeException("Invalid Access!!!!!!");
		}
	}
	
	@PostMapping("/google")
	public String loginWithGoogle(@RequestBody Map<String, String> request) {
	    String idToken = request.get("token");
	    return authService.loginWithGoogle(idToken);
	}
	
	@GetMapping("/validate")
	public String validateToken(@RequestParam String token) {
		authService.validateToken(token);
		return "!!Token is Valid!!";
	}
	
	@PutMapping("/change-password")
	public String changePassword(@RequestBody ChangePasswordRequest request) {
	    return authService.changePassword(request);
	}
}
