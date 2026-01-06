package com.identity_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.identity_service.dto.CreateUserRequest;
import com.identity_service.dto.UserResponse;
import com.identity_service.model.ERole;
import com.identity_service.service.impl.AuthServiceImpl;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AuthServiceImpl authService;
    
    /**
     * Create a new staff user (agent, claims officer, provider)
     * POST /admin/users
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse user = authService.createUserByAdmin(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    
    /**
     * Get all staff users (agents, officers, providers, admins)
     * GET /admin/users/staff
     */
    @GetMapping("/users/staff")
    public ResponseEntity<List<UserResponse>> getAllStaffUsers() {
        return ResponseEntity.ok(authService.getAllStaffUsers());
    }
    
    /**
     * Get all users
     * GET /admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
    
    /**
     * Get users by role
     * GET /admin/users/role/{role}
     * Valid roles: ROLE_AGENT, ROLE_CLAIMS_OFFICER, ROLE_PROVIDER, ROLE_USER, ROLE_ADMIN
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        try {
            ERole eRole = ERole.valueOf(role.toUpperCase());
            return ResponseEntity.ok(authService.getUsersByRole(eRole));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
    }
    
    /**
     * Suspend a user account
     * PUT /admin/users/{userId}/suspend
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<UserResponse> suspendUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(authService.suspendUser(userId));
    }
    
    /**
     * Activate a suspended user account
     * PUT /admin/users/{userId}/activate
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(authService.activateUser(userId));
    }
    
    /**
     * Update user details
     * PUT /admin/users/{userId}
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer userId, 
            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.updateUser(userId, request));
    }
    
    /**
     * Delete a user (use with caution)
     * DELETE /admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
