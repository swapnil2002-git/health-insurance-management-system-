package com.healthinsurance.identity.service;

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
import com.healthinsurance.identity.exception.UserAlreadyExistsException;
import com.healthinsurance.identity.repository.RoleRepository;
import com.healthinsurance.identity.repository.UserRepository;
import com.healthinsurance.identity.repository.UserSessionRepository;
import com.healthinsurance.identity.service.impl.AuthServiceImpl;
import com.healthinsurance.identity.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private IdentityAuditTrailService auditTrailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role customerRole;
    private Role underwriterRole;

    @BeforeEach
    void setUp() {
        customerRole = new Role();
        customerRole.setName("CUSTOMER");

        underwriterRole = new Role();
        underwriterRole.setName("UNDERWRITER");

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setRole(customerRole);
        testUser.setStatus("ACTIVE");
    }

    @Test
    void registerUser_Customer_SetsActiveStatus() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setPassword("Password123");
        req.setEmail("bob@example.com");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> authService.registerUser(req));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("ACTIVE", userCaptor.getValue().getStatus());
        assertEquals("CUSTOMER", userCaptor.getValue().getRole().getName());
    }

    @Test
    void registerUser_StaffRole_SetsPendingApprovalStatus() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("dan");
        req.setPassword("Password123");
        req.setEmail("dan@example.com");
        req.setRole("UNDERWRITER");

        when(userRepository.existsByUsername("dan")).thenReturn(false);
        when(userRepository.existsByEmail("dan@example.com")).thenReturn(false);
        when(roleRepository.findByName("UNDERWRITER")).thenReturn(Optional.of(underwriterRole));
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> authService.registerUser(req));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("PENDING_APPROVAL", userCaptor.getValue().getStatus());
        assertEquals("UNDERWRITER", userCaptor.getValue().getRole().getName());
    }

    @Test
    void registerUser_DuplicateUsername_ThrowsUserAlreadyExistsException() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("Password123");
        req.setEmail("unique@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ValidCredentialsActive_ReturnsLoginResponse() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("plainPassword");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken("alice", "CUSTOMER")).thenReturn("mock.jwt.token");

        LoginResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("mock.jwt.token", resp.getToken());
        assertEquals("alice", resp.getUsername());
        assertEquals("CUSTOMER", resp.getRole());
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void login_PendingApproval_ThrowsAccountPendingApprovalException() {
        testUser.setStatus("PENDING_APPROVAL");
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("plainPassword");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword123")).thenReturn(true);

        assertThrows(AccountPendingApprovalException.class, () -> authService.login(req));
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_InvalidUsername_ThrowsInvalidCredentialsException() {
        LoginRequest req = new LoginRequest();
        req.setUsername("unknown");
        req.setPassword("secret");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_InvalidPassword_ThrowsInvalidCredentialsException() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrongPassword");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void getPendingUsers_ReturnsPendingList() {
        testUser.setStatus("PENDING_APPROVAL");
        when(userRepository.findByStatus("PENDING_APPROVAL")).thenReturn(Collections.singletonList(testUser));

        List<UserSummaryResponse> pending = authService.getPendingUsers();

        assertNotNull(pending);
        assertEquals(1, pending.size());
        assertEquals("alice", pending.get(0).getUsername());
        assertEquals("PENDING_APPROVAL", pending.get(0).getStatus());
    }

    @Test
    void approveUser_ValidUser_SetsActiveAndAudits() {
        testUser.setStatus("PENDING_APPROVAL");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));

        authService.approveUser("alice");

        assertEquals("ACTIVE", testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
        verify(auditTrailService, times(1)).recordAudit(
                eq("USER_APPROVED"),
                eq("User"),
                any(),
                eq("PENDING_APPROVAL"),
                eq("ACTIVE"),
                eq("/api/auth/users/alice/approve"),
                isNull()
        );
    }

    @Test
    void rejectUser_ValidUser_SetsRejectedAndAudits() {
        testUser.setStatus("PENDING_APPROVAL");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));

        authService.rejectUser("alice");

        assertEquals("REJECTED", testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
        verify(auditTrailService, times(1)).recordAudit(
                eq("USER_REJECTED"),
                eq("User"),
                any(),
                eq("PENDING_APPROVAL"),
                eq("REJECTED"),
                eq("/api/auth/users/alice/reject"),
                isNull()
        );
    }

    @Test
    void assignUserRole_ValidRole_UpdatesRoleAndAudits() {
        Role adminRole = new Role();
        adminRole.setName("SYSTEM_ADMINISTRATOR");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("SYSTEM_ADMINISTRATOR")).thenReturn(Optional.of(adminRole));

        authService.assignUserRole("alice", "SYSTEM_ADMINISTRATOR");

        assertEquals("SYSTEM_ADMINISTRATOR", testUser.getRole().getName());
        assertEquals("ACTIVE", testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
        verify(auditTrailService, times(1)).recordAudit(
                eq("PERMISSION_CHANGED"),
                eq("UserRole"),
                any(),
                eq("CUSTOMER"),
                eq("SYSTEM_ADMINISTRATOR"),
                eq("/api/auth/users/alice/role"),
                isNull()
        );
    }
}
