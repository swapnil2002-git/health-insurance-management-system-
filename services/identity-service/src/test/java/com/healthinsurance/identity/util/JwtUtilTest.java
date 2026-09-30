package com.healthinsurance.identity.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final Long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void generateToken_ValidTokenGenerated() {
        String token = jwtUtil.generateToken("testuser", "CUSTOMER");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertEquals("CUSTOMER", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.structure"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // already expired
        String expiredToken = jwtUtil.generateToken("testuser", "CUSTOMER");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testGlobalExceptionHandler_StandardErrorResponse() {
        com.healthinsurance.identity.exception.GlobalExceptionHandler handler =
                new com.healthinsurance.identity.exception.GlobalExceptionHandler();
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        request.addHeader("X-Correlation-ID", "corr-test-123");

        // 1. InvalidCredentialsException -> 401 UNAUTHORIZED
        com.healthinsurance.identity.exception.InvalidCredentialsException credEx =
                new com.healthinsurance.identity.exception.InvalidCredentialsException("Invalid username or password");
        org.springframework.http.ResponseEntity<com.healthinsurance.identity.dto.ErrorResponse> resp401 = handler.handleInvalidCredentials(credEx, request);
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, resp401.getStatusCode());
        assertEquals(401, resp401.getBody().getStatus());
        assertEquals("UNAUTHORIZED", resp401.getBody().getErrorCode());
        assertEquals("corr-test-123", resp401.getBody().getCorrelationId());
        assertEquals("/api/auth/login", resp401.getBody().getPath());
        assertNotNull(resp401.getBody().getTimestamp());

        // 2. UserAlreadyExistsException -> 409 RESOURCE_CONFLICT
        com.healthinsurance.identity.exception.UserAlreadyExistsException existEx =
                new com.healthinsurance.identity.exception.UserAlreadyExistsException("User exists");
        org.springframework.http.ResponseEntity<com.healthinsurance.identity.dto.ErrorResponse> resp409 = handler.handleUserAlreadyExists(existEx, request);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT, resp409.getStatusCode());
        assertEquals(409, resp409.getBody().getStatus());
        assertEquals("RESOURCE_CONFLICT", resp409.getBody().getErrorCode());
        assertEquals("corr-test-123", resp409.getBody().getCorrelationId());

        // 3. AccessDeniedException -> 403 ACCESS_DENIED
        org.springframework.security.access.AccessDeniedException denyEx =
                new org.springframework.security.access.AccessDeniedException("Access denied");
        org.springframework.http.ResponseEntity<com.healthinsurance.identity.dto.ErrorResponse> resp403 = handler.handleAccessDenied(denyEx, request);
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, resp403.getStatusCode());
        assertEquals(403, resp403.getBody().getStatus());
        assertEquals("ACCESS_DENIED", resp403.getBody().getErrorCode());
        assertEquals("corr-test-123", resp403.getBody().getCorrelationId());

        // 4. Exception -> 500 INTERNAL_SERVER_ERROR
        org.springframework.mock.web.MockHttpServletRequest noHeaderRequest =
                new org.springframework.mock.web.MockHttpServletRequest();
        noHeaderRequest.setRequestURI("/api/auth/unknown");
        org.springframework.http.ResponseEntity<com.healthinsurance.identity.dto.ErrorResponse> resp500 = handler.handleGeneric(new RuntimeException("Crash"), noHeaderRequest);
        assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, resp500.getStatusCode());
        assertEquals(500, resp500.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", resp500.getBody().getErrorCode());
        assertNotNull(resp500.getBody().getCorrelationId());
        assertFalse(resp500.getBody().getCorrelationId().trim().isEmpty());
    }
}
