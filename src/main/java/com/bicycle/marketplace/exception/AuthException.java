package com.bicycle.marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for authentication-related errors.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    private final String errorCode;

    public AuthException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Common auth exception factory methods
    public static AuthException invalidCredentials() {
        return new AuthException("Invalid username or password", "INVALID_CREDENTIALS");
    }

    public static AuthException userNotFound() {
        return new AuthException("User not found", "USER_NOT_FOUND");
    }

    public static AuthException userDisabled() {
        return new AuthException("User account is disabled", "USER_DISABLED");
    }

    public static AuthException tokenExpired() {
        return new AuthException("Token has expired", "TOKEN_EXPIRED");
    }

    public static AuthException invalidToken() {
        return new AuthException("Invalid token", "INVALID_TOKEN");
    }

    public static AuthException emailAlreadyExists() {
        return new AuthException("Email already registered", "EMAIL_EXISTS");
    }

    public static AuthException usernameAlreadyExists() {
        return new AuthException("Username already taken", "USERNAME_EXISTS");
    }

    public static AuthException passwordMismatch() {
        return new AuthException("Passwords do not match", "PASSWORD_MISMATCH");
    }
}
