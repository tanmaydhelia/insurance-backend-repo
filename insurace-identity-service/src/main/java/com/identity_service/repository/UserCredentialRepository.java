package com.identity_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.identity_service.model.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
	Optional<UserCredential> findByName(String name);
    Optional<UserCredential> findByEmail(String email);
    Boolean existsByEmail(String email);
}