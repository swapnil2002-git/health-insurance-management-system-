package com.healthinsurance.policy.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.security.Key;

@Slf4j
@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null && attributes.getRequest().getHeader("Authorization") != null) {
            template.header("Authorization", attributes.getRequest().getHeader("Authorization"));
            log.debug("Propagating existing JWT Token via Feign");
        } else {
            log.info("No active HTTP Request found. Generating System JWT for internal Feign call.");
            template.header("Authorization", "Bearer " + generateSystemToken());
        }
    }

    private String generateSystemToken() {
        // FIXED: Base64 Decoding the secret so Quotation Service accepts it!
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .setSubject("policy-service-system")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}