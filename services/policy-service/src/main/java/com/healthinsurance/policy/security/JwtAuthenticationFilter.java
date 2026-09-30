package com.healthinsurance.policy.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
            
        String header = request.getHeader("Authorization");
        
        if (header == null) {
            log.warn("JWT Filter: No Authorization header found for URI: {}", request.getRequestURI());
        } else if (!header.startsWith("Bearer ")) {
            log.warn("JWT Filter: Authorization header found, but does not start with 'Bearer '");
        } else {
            String token = header.substring(7);
            log.info("JWT Filter: Bearer token found. Attempting validation...");
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);
                    log.info("JWT Filter: Token is valid. Subject: {}, Role: {}", username, role);

                    List<SimpleGrantedAuthority> authorities = (role != null) 
                            ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            : Collections.emptyList();
                    
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.error("JWT Filter: Token validation failed (Expired or invalid signature).");
                }
            } catch (Exception e) {
                log.error("JWT Filter: Exception during token validation: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}