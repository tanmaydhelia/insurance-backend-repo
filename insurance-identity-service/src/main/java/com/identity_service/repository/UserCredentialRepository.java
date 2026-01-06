package com.identity_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.identity_service.model.ERole;
import com.identity_service.model.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
	Optional<UserCredential> findById(Integer id);	
	Optional<UserCredential> findByName(String name);
    Optional<UserCredential> findByEmail(String email);
    Boolean existsByEmail(String email);
    
    // Admin methods
    List<UserCredential> findByRole(ERole role);
    List<UserCredential> findByRoleIn(List<ERole> roles);
    List<UserCredential> findByRoleNot(ERole role);
    List<UserCredential> findByActive(Boolean active);
    List<UserCredential> findByRoleAndActive(ERole role, Boolean active);
}