package com.insurance_claims.util;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private final String secretKey = "tanmaydheliatanmaydheliatanmaydheliatanmaydheliatanmaydheliatanmaydheliatanmaydhelia";
    
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }
    
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
