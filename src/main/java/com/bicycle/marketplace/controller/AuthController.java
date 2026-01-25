package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.auth.LoginRequest;
import com.bicycle.marketplace.dto.request.auth.RefreshTokenRequest;
import com.bicycle.marketplace.dto.request.auth.RegisterRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.auth.LoginResponse;
import com.bicycle.marketplace.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles login, registration, token refresh, and user info retrieval.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate user and return JWT tokens.
     *
     * @param loginRequest the login credentials
     * @return ResponseEntity containing login response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the registration details
     * @return ResponseEntity containing login response with tokens
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        LoginResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param request the refresh token request
     * @return ResponseEntity containing new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Get current authenticated user info.
     *
     * @param userDetails the authenticated user details
     * @return ResponseEntity containing user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
