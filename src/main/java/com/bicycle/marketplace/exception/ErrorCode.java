package com.bicycle.marketplace.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USERNAME_ALREADY_EXISTS(1001, "Username already exists"),
    USER_NOT_FOUND(1002, "User not found"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    PASSWORD_INVALID(1003, "Password is invalid"),
    INVALID_KEY(1004, "Invalid message key"),
    LISTING_NOT_FOUND(1005, "Posting/Listing not found"),
    USER_INVALID_AUTHENTICATIED(1006, "User authentication failed")
    ;

    private int code;
    private String message;
}
