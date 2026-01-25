package com.bicycle.marketplace.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login/authentication response containing JWT tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
    }

    /**
     * Create a LoginResponse with Bearer token type.
     */
    public static LoginResponse of(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}
