package com.bicycle.marketplace.service;

import com.bicycle.marketplace.dto.request.auth.LoginRequest;
import com.bicycle.marketplace.dto.request.auth.RegisterRequest;
import com.bicycle.marketplace.dto.response.auth.LoginResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticate user and generate JWT tokens.
     *
     * @param loginRequest the login credentials
     * @return LoginResponse containing access and refresh tokens
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Register a new user.
     *
     * @param registerRequest the registration details
     * @return LoginResponse containing access and refresh tokens for the new user
     */
    LoginResponse register(RegisterRequest registerRequest);

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken the refresh token
     * @return LoginResponse containing new access token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * Get current authenticated user info.
     *
     * @param username the username
     * @return LoginResponse.UserInfo containing user details
     */
    LoginResponse.UserInfo getCurrentUser(String username);
}
