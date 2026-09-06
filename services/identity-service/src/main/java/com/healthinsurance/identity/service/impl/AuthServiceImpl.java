package com.healthinsurance.identity.service.impl;

import com.healthinsurance.identity.dto.LoginRequest;
import com.healthinsurance.identity.dto.LoginResponse;
import com.healthinsurance.identity.dto.RegisterRequest;
import com.healthinsurance.identity.entity.Role;
import com.healthinsurance.identity.entity.User;
import com.healthinsurance.identity.entity.UserSession;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        Role role = roleRepository.findByName("CUSTOMER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("CUSTOMER");
            newRole.setDescription("Default Customer Role");
            return roleRepository.save(newRole);
        });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("ACTIVE");

        userRepository.save(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password!");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());

        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken(token);
        session.setExpiresAt(Instant.now().plusMillis(86400000)); // 24 hours
        userSessionRepository.save(session);

        return new LoginResponse(token, user.getUsername(), user.getRole().getName());
    }
}