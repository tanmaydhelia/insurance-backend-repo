package com.identity_service.dto;

import com.identity_service.model.ERole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private ERole role;  // ROLE_AGENT, ROLE_CLAIMS_OFFICER, ROLE_PROVIDER, ROLE_ADMIN
}
