package com.insurance_gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.insurance_gateway.util.JwtUtil;

import io.jsonwebtoken.Claims;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config>{
	@Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;
    
    public static class Config{}
    
    public AuthenticationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
    	return ((exchange, chain) -> {
    		ServerHttpRequest request = exchange.getRequest();
    		
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                
                try {
                    jwtUtil.validateToken(authHeader);
                    
                    Claims claims = jwtUtil.getClaims(authHeader);
                    String role = claims.get("roles", String.class);
                    String username = claims.getSubject();

                    // Restrict creating Plans to ADMINs only
                    String path = request.getURI().getPath();
                    HttpMethod method = request.getMethod();
                    
                    if (path.contains("/api/plans") && method == HttpMethod.POST) {
                        if (!"ROLE_ADMIN".equals(role)) {
                            throw new RuntimeException("Forbidden: Admin access required to create plans");
                        }
                    }
                    
//                    request.mutate()
//                    .header("X-Auth-User", username)
//                    .header("X-Auth-Roles", role)
//                    .build();
                    
                } catch (Exception e) {
                    throw new RuntimeException("Unauthorized Access");
                }
                
                
                
            }
            return chain.filter(exchange);
    	});
    }
}
