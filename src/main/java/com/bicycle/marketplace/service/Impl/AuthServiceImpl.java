package com.bicycle.marketplace.service.impl;

import com.bicycle.marketplace.dto.request.auth.LoginRequest;
import com.bicycle.marketplace.dto.request.auth.RegisterRequest;
import com.bicycle.marketplace.dto.response.auth.LoginResponse;
import com.bicycle.marketplace.entity.Role;
import com.bicycle.marketplace.entity.User;
import com.bicycle.marketplace.exception.AuthException;
import com.bicycle.marketplace.repository.UserJpaRepository;
import com.bicycle.marketplace.security.CustomUserDetailsService;
import com.bicycle.marketplace.security.JWTTokenProvider;
import com.bicycle.marketplace.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService for authentication operations.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
            UserJpaRepository userJpaRepository,
            PasswordEncoder passwordEncoder,
            JWTTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Load user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Get user info
        User user = userJpaRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(AuthException::userNotFound);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                userInfo);
    }

    @Override
    public LoginResponse register(RegisterRequest registerRequest) {
        // Validate password match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw AuthException.passwordMismatch();
        }

        // Check if username exists
        if (userJpaRepository.existsByUsername(registerRequest.getUsername())) {
            throw AuthException.usernameAlreadyExists();
        }

        // Check if email exists
        if (userJpaRepository.existsByEmail(registerRequest.getEmail())) {
            throw AuthException.emailAlreadyExists();
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhoneNumber())
                .role(Role.USER)
                .enabled(true)
                .status("ACTIVE")
                .walletBalance(0.0f)
                .build();

        userJpaRepository.save(user);

        // Auto login after registration
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                userInfo);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw AuthException.invalidToken();
        }

        // Extract username from refresh token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Load user details
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);

        // Get user info
        User user = userJpaRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        return LoginResponse.of(
                newAccessToken,
                refreshToken, // Return the same refresh token
                jwtTokenProvider.getAccessTokenExpiration(),
                userInfo);
    }

    @Override
    public LoginResponse.UserInfo getCurrentUser(String username) {
        User user = userJpaRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);

        return LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
