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
    USERNAME_REQUIRED(1010, "Username is required"),
    PASSWORD_REQUIRED(1011, "Password is required"),
    USERNAME_INVALID_LENGTH(1012, "Username must be between 3 and 50 characters"),
    FULL_NAME_REQUIRED(1013, "Full name is required"),
    FULL_NAME_MAX_LENGTH(1014, "Full name must not exceed 100 characters"),
    EMAIL_REQUIRED(1015, "Email is required"),
    EMAIL_INVALID(1016, "Email format is invalid"),
    PHONE_MAX_LENGTH(1017, "Phone must not exceed 20 characters"),
    STATUS_MAX_LENGTH(1018, "Status must not exceed 50 characters"),
    LISTING_NOT_FOUND(1005, "Posting/Listing not found"),
    USER_INVALID_AUTHENTICATIED(1006, "User authentication failed"),
    EVENT_NOT_FOUND(1007, "Event not found"),
    POSTING_SELLER_ID_REQUIRED(1008, "sellerId is required"),
    POSTING_EVENT_ID_REQUIRED(1009, "eventId is required"),
    CHECKIN_NOT_FOUND(1010, "Check-in record not found"),
    DEPOSIT_NOT_FOUND(1011, "Deposit record not found"),
    DEPOSITSETTLEMENT_NOT_FOUND(1012, "Deposit settlement record not found"),
    DISPUTE_NOT_FOUND(1013, "Dispute record not found"),
    INSPECTIONREPORT_NOT_FOUND(1014, "Inspection report not found"),
    RESERVATION_NOT_FOUND(1015, "Reservation not found"),
    TRANSACTION_NOT_FOUND(1016, "Transaction not found")
    ;

    private int code;
    private String message;
}
