package com.identity_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic user information DTO for public endpoints.
 * Contains only non-sensitive user details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfo {
    private Integer id;
    private String name;
    private String email;
}
