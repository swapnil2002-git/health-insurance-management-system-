package com.healthinsurance.premium.config;

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

import java.security.Key;
import java.util.Date;

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
            log.debug("Propagating existing JWT Token via Feign in Premium Service");
        } else {
            log.info("No active HTTP Request context. Generating System JWT for internal Feign call from Premium Service.");
            template.header("Authorization", "Bearer " + generateSystemToken());
        }
    }

    private String generateSystemToken() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject("premium-service-system")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}