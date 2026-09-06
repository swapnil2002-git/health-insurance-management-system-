package com.healthinsurance.identity.service;
import com.healthinsurance.identity.dto.LoginRequest;
import com.healthinsurance.identity.dto.LoginResponse;
import com.healthinsurance.identity.dto.RegisterRequest;

public interface AuthService {
    void registerUser(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}