package com.healthinsurance.identity.controller;

import com.healthinsurance.identity.dto.LoginRequest;
import com.healthinsurance.identity.dto.LoginResponse;
import com.healthinsurance.identity.dto.RegisterRequest;
import com.healthinsurance.identity.dto.UserSummaryResponse;
import com.healthinsurance.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        String role = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim().toUpperCase()
                : "CUSTOMER";
        if ("CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully! Account is active.");
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered with role " + role + "! Account is pending administrator approval.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Step 3.13 Test Endpoint: Accessible with a valid token
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Principal principal) {
        return ResponseEntity.ok("Hello, " + principal.getName() + "! You successfully passed JWT validation and RBAC checking!");
    }

    // Admin endpoint: List all pending user registrations
    @GetMapping("/users/pending")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getPendingUsers() {
        return ResponseEntity.ok(authService.getPendingUsers());
    }

    // Admin endpoint: Approve a pending user account
    @PutMapping("/users/{username}/approve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable("username") String username) {
        authService.approveUser(username);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User " + username + " approved successfully! Account is now active.");
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Reject a pending user account
    @PutMapping("/users/{username}/reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable("username") String username) {
        authService.rejectUser(username);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User " + username + " registration rejected.");
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Assign or change user role
    @PutMapping("/users/{username}/role")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> assignRole(@PathVariable("username") String username, @RequestParam("role") String role) {
        authService.assignUserRole(username, role);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated to " + role.toUpperCase() + " for user: " + username);
        return ResponseEntity.ok(response);
    }
}