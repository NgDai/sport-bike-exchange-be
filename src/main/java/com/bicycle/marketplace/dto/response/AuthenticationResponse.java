package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class AuthenticationResponse {
    String token;
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String walletBalance;
    private String status;
}
