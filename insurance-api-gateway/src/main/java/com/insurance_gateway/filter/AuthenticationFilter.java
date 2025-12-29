package com.insurance_gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.insurance_gateway.util.JwtUtil;

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
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    throw new RuntimeException("Unauthorized Access");
                }
                
//                String token = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).substring(7);
//                try {
//                    jwtUtil.validateToken(token);
//                    
//                    String path = exchange.getRequest().getURI().getPath();
//                    if (path.contains("/api/airline")) { 
//                        String role = jwtUtil.getClaims(token).get("role", String.class);
//                        if (!"ROLE_ADMIN".equals(role)) {
//                            throw new RuntimeException("Forbidden: Admin access required");
//                        }
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException("Unauthorized Access");
//                }
                
            }
            return chain.filter(exchange);
    	});
    }
}
