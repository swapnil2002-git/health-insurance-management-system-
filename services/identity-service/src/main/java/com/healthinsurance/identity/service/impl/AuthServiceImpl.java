package com.healthinsurance.identity.service.impl;

import com.healthinsurance.identity.audit.IdentityAuditTrailService;
import com.healthinsurance.identity.dto.LoginRequest;
import com.healthinsurance.identity.dto.LoginResponse;
import com.healthinsurance.identity.dto.RegisterRequest;
import com.healthinsurance.identity.dto.UserSummaryResponse;
import com.healthinsurance.identity.entity.Role;
import com.healthinsurance.identity.entity.User;
import com.healthinsurance.identity.entity.UserSession;
import com.healthinsurance.identity.exception.AccountPendingApprovalException;
import com.healthinsurance.identity.exception.InvalidCredentialsException;
import com.healthinsurance.identity.exception.ResourceNotFoundException;
import com.healthinsurance.identity.exception.UserAlreadyExistsException;
import com.healthinsurance.identity.repository.RoleRepository;
import com.healthinsurance.identity.repository.UserRepository;
import com.healthinsurance.identity.repository.UserSessionRepository;
import com.healthinsurance.identity.service.AuthService;
import com.healthinsurance.identity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IdentityAuditTrailService auditTrailService;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists!");
        }

        String requestedRole = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim().toUpperCase()
                : "CUSTOMER";

        Role role = roleRepository.findByName(requestedRole).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(requestedRole);
            newRole.setDescription(requestedRole + " Role");
            return roleRepository.save(newRole);
        });

        // Regular customers are auto-approved; elevated/staff roles require admin approval
        String initialStatus = "CUSTOMER".equals(requestedRole) ? "ACTIVE" : "PENDING_APPROVAL";

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(initialStatus);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password!");
        }

        if ("PENDING_APPROVAL".equalsIgnoreCase(user.getStatus())) {
            throw new AccountPendingApprovalException("Your account is pending administrator approval.");
        }
        if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
            throw new AccountPendingApprovalException("Your account registration request has been rejected.");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AccountPendingApprovalException("Your account is not active. Status: " + user.getStatus());
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());

        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken(token);
        session.setExpiresAt(Instant.now().plusMillis(86400000)); // 24 hours
        userSessionRepository.save(session);

        return new LoginResponse(token, user.getUsername(), user.getRole().getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getPendingUsers() {
        return userRepository.findByStatus("PENDING_APPROVAL").stream()
                .map(u -> UserSummaryResponse.builder()
                        .userId(u.getUserId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().getName() : null)
                        .status(u.getStatus())
                        .createdAt(u.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        String oldStatus = user.getStatus();
        user.setStatus("ACTIVE");
        userRepository.save(user);

        auditTrailService.recordAudit(
                "USER_APPROVED",
                "User",
                user.getUserId() != null ? user.getUserId().toString() : username,
                oldStatus,
                "ACTIVE",
                "/api/auth/users/" + username + "/approve",
                null
        );
    }

    @Override
    @Transactional
    public void rejectUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        String oldStatus = user.getStatus();
        user.setStatus("REJECTED");
        userRepository.save(user);

        auditTrailService.recordAudit(
                "USER_REJECTED",
                "User",
                user.getUserId() != null ? user.getUserId().toString() : username,
                oldStatus,
                "REJECTED",
                "/api/auth/users/" + username + "/reject",
                null
        );
    }

    @Override
    @Transactional
    public void assignUserRole(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        String normalizedRole = roleName.trim().toUpperCase();
        Role role = roleRepository.findByName(normalizedRole).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(normalizedRole);
            newRole.setDescription(normalizedRole + " Role");
            return roleRepository.save(newRole);
        });

        String previousRole = user.getRole() != null ? user.getRole().getName() : null;
        user.setRole(role);
        user.setStatus("ACTIVE");
        userRepository.save(user);

        auditTrailService.recordAudit(
                "PERMISSION_CHANGED",
                "UserRole",
                user.getUserId() != null ? user.getUserId().toString() : username,
                previousRole,
                role.getName(),
                "/api/auth/users/" + username + "/role",
                null
        );
    }
}