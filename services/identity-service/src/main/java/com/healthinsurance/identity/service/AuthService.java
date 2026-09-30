package com.healthinsurance.identity.service;
import com.healthinsurance.identity.dto.LoginRequest;
import com.healthinsurance.identity.dto.LoginResponse;
import com.healthinsurance.identity.dto.RegisterRequest;
import com.healthinsurance.identity.dto.UserSummaryResponse;

import java.util.List;

public interface AuthService {
    void registerUser(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    List<UserSummaryResponse> getPendingUsers();
    void approveUser(String username);
    void rejectUser(String username);
    void assignUserRole(String username, String roleName);
}